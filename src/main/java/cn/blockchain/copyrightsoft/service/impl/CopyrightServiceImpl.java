package cn.blockchain.copyrightsoft.service.impl;

import cn.blockchain.copyrightsoft.contracts.CopyrightRegistry;
import cn.blockchain.copyrightsoft.dto.ApplyCopyrightRequest;
import cn.blockchain.copyrightsoft.dto.QueryResult;
import cn.blockchain.copyrightsoft.entity.CopyrightRecord;
import cn.blockchain.copyrightsoft.entity.FileStorage;
import cn.blockchain.copyrightsoft.mapper.CopyrightRecordMapper;
import cn.blockchain.copyrightsoft.mapper.FileStorageMapper;
import cn.blockchain.copyrightsoft.service.CopyrightService;
import cn.blockchain.copyrightsoft.service.AuthService;
import cn.blockchain.copyrightsoft.utils.FileHashUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CopyrightServiceImpl implements CopyrightService {

    private final Client client;
    private final CryptoKeyPair cryptoKeyPair;
    private final CopyrightRecordMapper recordMapper;
    private final FileStorageMapper fileStorageMapper;
    private final MinioClient minioClient;

    @Autowired
    private AuthService authService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${fisco.bcos.contract-address}")
    private String contractAddress;

    public CopyrightServiceImpl(Client client,
                               CryptoKeyPair cryptoKeyPair,
                               CopyrightRecordMapper recordMapper,
                               FileStorageMapper fileStorageMapper,
                               MinioClient minioClient) {
        this.client = client;
        this.cryptoKeyPair = cryptoKeyPair;
        this.recordMapper = recordMapper;
        this.fileStorageMapper = fileStorageMapper;
        this.minioClient = minioClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyCopyright(MultipartFile file, ApplyCopyrightRequest request) {
        try {
            byte[] fileBytes = file.getBytes();

            String fileHash = FileHashUtils.calculateSHA256(fileBytes);

            log.info("=== 版权存证开始 ===");
            log.info("文件名: {}", file.getOriginalFilename());
            log.info("文件大小: {} bytes", file.getSize());
            log.info("文件哈希: {}", fileHash);
            log.info("软件名称: {}", request.getSoftwareName());
            log.info("合约地址: {}", contractAddress);
            log.info("群组ID: {}", client.getGroup());

            LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CopyrightRecord::getFileHash, fileHash);
            long dbCount = recordMapper.selectCount(wrapper);
            log.info("数据库中该哈希的记录数: {}", dbCount);

            if (dbCount > 0) {
                throw new RuntimeException("该文件已存在版权记录(数据库中存在)");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String storedFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(storedFilename)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            FileStorage fileStorage = new FileStorage();
            fileStorage.setOriginalFilename(originalFilename);
            fileStorage.setStoredFilename(storedFilename);
            fileStorage.setFileSize(file.getSize());
            fileStorage.setFileHash(fileHash);
            fileStorage.setStoragePath(bucketName + "/" + storedFilename);
            fileStorageMapper.insert(fileStorage);

            log.info("=== 区块链交易准备 ===");
            log.info("配置的合约地址: {}", contractAddress);
            log.info("调用者地址: {}", cryptoKeyPair.getAddress());
            log.info("客户端群组: {}", client.getGroup());

            // 确保合约地址格式正确
            String normalizedAddress = contractAddress;
            if (!normalizedAddress.startsWith("0x") && normalizedAddress.length() > 20) {
                // 可能是十进制格式,尝试转换
                try {
                    java.math.BigInteger bigInt = new java.math.BigInteger(normalizedAddress);
                    normalizedAddress = "0x" + bigInt.toString(16);
                    log.info("地址格式转换: {} -> {}", contractAddress, normalizedAddress);
                } catch (Exception e) {
                    log.warn("地址转换失败,使用原始地址: {}", e.getMessage());
                }
            }

            CopyrightRegistry contract = CopyrightRegistry.load(normalizedAddress, client, cryptoKeyPair);
            log.info("加载后的合约对象地址: {}", contract.getContractAddress());
            log.info("文件哈希: {}", fileHash);
            log.info("软件名称: {}", request.getSoftwareName());
            log.info("开始执行区块链交易, 合约地址: {}, 文件哈希: {}", contractAddress, fileHash);

            try {
                byte[] hashBytes = FileHashUtils.hexStringToBytes(fileHash);
                log.info("查询区块链, 哈希字节数组长度: {}", hashBytes.length);

                var existingRecord = contract.queryByHash(hashBytes);
                log.info("区块链查询结果详情:");
                log.info("  - 软件名称: '{}'", existingRecord.getValue1());
                log.info("  - 拥有者地址: '{}'", existingRecord.getValue2());
                log.info("  - 时间戳: {}", existingRecord.getValue3());
                log.info("  - 描述: '{}'", existingRecord.getValue4());
                log.info("  - 是否已注册: {}", existingRecord.getValue5());

                if (existingRecord != null && existingRecord.getValue5()) {
                    String errorMsg = String.format(
                            "该文件哈希已在区块链上注册 - 软件名: '%s', 拥有者: %s, 时间戳: %d",
                            existingRecord.getValue1(),
                            existingRecord.getValue2(),
                            existingRecord.getValue3()
                    );
                    log.warn(errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                log.error("查询区块链记录失败: {}", e.getMessage(), e);
                throw new RuntimeException("查询区块链失败: " + e.getMessage(), e);
            }

            TransactionReceipt receipt = contract.applyForCopyright(
                    FileHashUtils.hexStringToBytes(fileHash),
                    request.getSoftwareName(),
                    request.getDescription()
            );

            log.info("交易回执 - 状态码: {}, 消息: '{}', 交易哈希: {}, GasUsed: {}, 区块号: {}",
                    receipt.getStatus(), receipt.getMessage(),
                    receipt.getTransactionHash(), receipt.getGasUsed(),
                    receipt.getBlockNumber());

            if (!receipt.isStatusOK()) {
                String errorMsg = String.format("区块链交易失败 - 状态码: %s, 交易哈希: %s",
                        receipt.getStatus(), receipt.getTransactionHash());

                if (receipt.getStatus() == 19) {
                    errorMsg += " (原因: 该文件哈希已存在版权记录)";
                }

                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            CopyrightRecord record = new CopyrightRecord();
            record.setFileHash(fileHash);
            record.setSoftwareName(request.getSoftwareName());
            record.setDescription(request.getDescription());
            record.setOwnerAddress(receipt.getFrom());
            record.setTxHash(receipt.getTransactionHash());
            record.setBlockNumber(receipt.getBlockNumber().longValue());
            // 设置当前用户ID
            Long userId = authService.getCurrentUserId();
            record.setUserId(userId != null ? userId : 0L); // 如果无法获取用户ID，设置为0
            recordMapper.insert(record);

            log.info("版权存证成功, 文件哈希: {}, 交易哈希: {}", fileHash, receipt.getTransactionHash());
            return receipt.getTransactionHash();

        } catch (Exception e) {
            log.error("版权存证失败", e);
            throw new RuntimeException("版权存证失败: " + e.getMessage());
        }
    }

    @Override
    public QueryResult queryByHash(String fileHash) {
        LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CopyrightRecord::getFileHash, fileHash);
        CopyrightRecord record = recordMapper.selectOne(wrapper);

        if (record == null) {
            return null;
        }

        return convertToQueryResult(record);
    }

    @Override
    public QueryResult queryById(Long id) {
        CopyrightRecord record = recordMapper.selectById(id);
        if (record == null) {
            return null;
        }
        return convertToQueryResult(record);
    }

    @Override
    public Page<QueryResult> getMyRecords(Integer page, Integer size, String keyword) {
        Long userId = authService.getCurrentUserId();

        LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CopyrightRecord::getUserId, userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(CopyrightRecord::getSoftwareName, keyword);
        }

        wrapper.orderByDesc(CopyrightRecord::getCreatedAt);

        Page<CopyrightRecord> recordPage = new Page<>(page, size);
        Page<CopyrightRecord> resultPage = recordMapper.selectPage(recordPage, wrapper);

        Page<QueryResult> queryResultPage = new Page<>(page, size, resultPage.getTotal());
        List<QueryResult> queryResults = resultPage.getRecords().stream()
                .map(this::convertToQueryResult)
                .collect(Collectors.toList());
        queryResultPage.setRecords(queryResults);

        return queryResultPage;
    }

    @Override
    public Page<QueryResult> getAllCopyrights(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(CopyrightRecord::getSoftwareName, keyword)
                    .or()
                    .like(CopyrightRecord::getFileHash, keyword);
        }

        wrapper.orderByDesc(CopyrightRecord::getCreatedAt);

        Page<CopyrightRecord> recordPage = new Page<>(page, size);
        Page<CopyrightRecord> resultPage = recordMapper.selectPage(recordPage, wrapper);

        Page<QueryResult> queryResultPage = new Page<>(page, size, resultPage.getTotal());
        List<QueryResult> queryResults = resultPage.getRecords().stream()
                .map(this::convertToQueryResult)
                .collect(Collectors.toList());
        queryResultPage.setRecords(queryResults);

        return queryResultPage;
    }

    private QueryResult convertToQueryResult(CopyrightRecord record) {
        QueryResult result = new QueryResult();
        result.setId(record.getId());
        result.setFileHash(record.getFileHash());
        result.setSoftwareName(record.getSoftwareName());
        result.setDescription(record.getDescription());
        result.setOwnerAddress(record.getOwnerAddress());
        result.setTxHash(record.getTxHash());
        result.setBlockNumber(record.getBlockNumber());
        result.setCreatedAt(record.getCreatedAt());
        result.setUpdatedAt(record.getUpdatedAt());
        result.setUserId(record.getUserId());
        return result;
    }
}
