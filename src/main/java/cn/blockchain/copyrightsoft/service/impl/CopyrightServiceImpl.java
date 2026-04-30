package cn.blockchain.copyrightsoft.service.impl;

import cn.blockchain.copyrightsoft.contracts.SoftwareEvidenceAnchor;
import cn.blockchain.copyrightsoft.auth.AuthDomainRules;
import cn.blockchain.copyrightsoft.dto.ApplyCopyrightRequest;
import cn.blockchain.copyrightsoft.dto.ApplicationStatusResponse;
import cn.blockchain.copyrightsoft.dto.ApplicationSubmitResponse;
import cn.blockchain.copyrightsoft.dto.QueryResult;
import cn.blockchain.copyrightsoft.entity.CopyrightApplication;
import cn.blockchain.copyrightsoft.entity.CopyrightEvidence;
import cn.blockchain.copyrightsoft.entity.CopyrightRecord;
import cn.blockchain.copyrightsoft.entity.FileStorage;
import cn.blockchain.copyrightsoft.entity.OnchainTx;
import cn.blockchain.copyrightsoft.mapper.CopyrightApplicationMapper;
import cn.blockchain.copyrightsoft.mapper.CopyrightEvidenceMapper;
import cn.blockchain.copyrightsoft.entity.User;
import cn.blockchain.copyrightsoft.mapper.CopyrightRecordMapper;
import cn.blockchain.copyrightsoft.mapper.FileStorageMapper;
import cn.blockchain.copyrightsoft.mapper.OnchainTxMapper;
import cn.blockchain.copyrightsoft.service.CopyrightService;
import cn.blockchain.copyrightsoft.service.AuthService;
import cn.blockchain.copyrightsoft.utils.ArchiveFingerprintUtils;
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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
/**
 * 版权申请与上链核心服务。
 * <p>
 * 该服务负责完整的业务链路：文件摘要计算 -> 相似度评估 -> 申请落库 -> 证据落库/对象存储 -> 区块链上链。
 * 设计上将“人工复核”与“自动上链”统一为同一流程，并通过 application.status 与 onchain_tx.status 双轨记录业务状态。
 */
public class CopyrightServiceImpl implements CopyrightService {
    private static final int HIGH_RISK_THRESHOLD = 95;
    private static final int MEDIUM_RISK_THRESHOLD = 85;
    private static final int SIMILARITY_SCAN_LIMIT = 200;

    private final Client client;
    private final CryptoKeyPair cryptoKeyPair;
    private final CopyrightRecordMapper recordMapper;
    private final CopyrightApplicationMapper applicationMapper;
    private final CopyrightEvidenceMapper evidenceMapper;
    private final OnchainTxMapper onchainTxMapper;
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
                               CopyrightApplicationMapper applicationMapper,
                               CopyrightEvidenceMapper evidenceMapper,
                               OnchainTxMapper onchainTxMapper,
                               FileStorageMapper fileStorageMapper,
                               MinioClient minioClient) {
        this.client = client;
        this.cryptoKeyPair = cryptoKeyPair;
        this.recordMapper = recordMapper;
        this.applicationMapper = applicationMapper;
        this.evidenceMapper = evidenceMapper;
        this.onchainTxMapper = onchainTxMapper;
        this.fileStorageMapper = fileStorageMapper;
        this.minioClient = minioClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyCopyright(MultipartFile file, ApplyCopyrightRequest request) {
        ApplicationSubmitResponse response = submitApplication(file, request);
        return response.getTxHash();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplicationSubmitResponse submitApplication(MultipartFile file, ApplyCopyrightRequest request) {
        try {
            // 先生成多种指纹：fileHash 用于精确去重，normalized/semantic 用于近似检测与风险判定。
            byte[] fileBytes = file.getBytes();
            String fileHash = FileHashUtils.calculateSHA256(fileBytes);
            ArchiveFingerprintUtils.ArchiveFingerprint fingerprint = ArchiveFingerprintUtils.buildFingerprints(fileBytes);
            String normalizedHash = fingerprint.normalizedHash();
            String semanticHash = fingerprint.semanticHash();
            String metadataRaw = request.getSoftwareName() + "|" + (request.getDescription() == null ? "" : request.getDescription());
            String metadataHash = FileHashUtils.calculateSHA256(metadataRaw.getBytes(StandardCharsets.UTF_8));
            String evidenceRootHash = FileHashUtils.calculateSHA256((fileHash + "|" + metadataHash).getBytes(StandardCharsets.UTF_8));
            String applicationNo = "APP-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            log.info("=== 版权存证开始 ===");
            log.info("文件名: {}", file.getOriginalFilename());
            log.info("文件大小: {} bytes", file.getSize());
            log.info("文件哈希: {}", fileHash);
            log.info("规范化哈希: {}", normalizedHash);
            log.info("语义哈希: {}", semanticHash);
            log.info("申请编号: {}", applicationNo);
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

            LambdaQueryWrapper<CopyrightEvidence> normalizedWrapper = new LambdaQueryWrapper<>();
            normalizedWrapper.eq(CopyrightEvidence::getNormalizedHash, normalizedHash)
                    .last("limit 1");
            CopyrightEvidence existingNormalized = evidenceMapper.selectOne(normalizedWrapper);
            if (existingNormalized != null) {
                throw new RuntimeException("该规范化压缩包已存在存证记录");
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

            User currentUser = authService.getCurrentUser();
            Long userId = authService.getCurrentUserId();
            if (currentUser != null
                    && AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE.equals(currentUser.getAccountType())
                    && AuthDomainRules.ENTERPRISE_ROLE_LEGAL.equals(currentUser.getEnterpriseRole())) {
                throw new RuntimeException("企业法务账号不允许提交上链申请");
            }

            CopyrightApplication application = new CopyrightApplication();
            application.setApplicationNo(applicationNo);
            application.setSoftwareName(request.getSoftwareName());
            application.setDescription(request.getDescription());
            application.setUserId(userId != null ? userId : 0L);
            if (currentUser != null) {
                application.setSubjectType(currentUser.getAccountType());
                if (AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE.equals(currentUser.getAccountType())) {
                    application.setSubjectId(currentUser.getEnterpriseId());
                } else {
                    application.setSubjectId(currentUser.getId());
                }
                application.setSubjectName(currentUser.getDisplaySubjectName());
            }
            SimilarityDecision similarityDecision = evaluateSimilarity(semanticHash, currentUser);
            application.setRiskLevel(similarityDecision.riskLevel());
            application.setRiskReason(similarityDecision.riskReason());
            application.setSimilarityScore(similarityDecision.score());
            application.setStatus(similarityDecision.requireReview() ? "PENDING_REVIEW" : "AUTO_CHECKED");
            applicationMapper.insert(application);

            CopyrightEvidence evidence = new CopyrightEvidence();
            evidence.setApplicationId(application.getId());
            evidence.setFileStorageId(fileStorage.getId());
            evidence.setFileHash(fileHash);
            evidence.setMetadataHash(metadataHash);
            evidence.setEvidenceRootHash(evidenceRootHash);
            evidence.setNormalizedHash(normalizedHash);
            evidence.setSemanticHash(semanticHash);
            evidenceMapper.insert(evidence);

            ApplicationSubmitResponse response = new ApplicationSubmitResponse();
            response.setApplicationNo(applicationNo);
            if (similarityDecision.requireReview()) {
                // 命中中高风险时不直接上链，先进入人工复核，避免跨主体相似代码误登记。
                response.setStatus("PENDING_REVIEW");
                response.setTxHash(null);
                log.info("申请进入人工审核, 申请号: {}, 评分: {}, 原因: {}",
                        applicationNo, similarityDecision.score(), similarityDecision.riskReason());
                return response;
            }

            String txHash = processOnchain(application, evidence, request, currentUser, userId, similarityDecision, null, null);
            response.setStatus("ONCHAIN_SUCCESS");
            response.setTxHash(txHash);
            log.info("版权存证成功, 申请号: {}, 交易哈希: {}", applicationNo, txHash);
            return response;

        } catch (Exception e) {
            log.error("版权存证失败", e);
            throw new RuntimeException("版权存证失败: " + e.getMessage());
        }
    }

    @Override
    public ApplicationStatusResponse getApplicationStatus(String applicationNo) {
        // 查询申请主状态，并拼接最新链上交易结果，前端据此展示“处理中/成功/失败”。
        LambdaQueryWrapper<CopyrightApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(CopyrightApplication::getApplicationNo, applicationNo);
        CopyrightApplication application = applicationMapper.selectOne(appWrapper);
        if (application == null) {
            return null;
        }

        LambdaQueryWrapper<OnchainTx> txWrapper = new LambdaQueryWrapper<>();
        txWrapper.eq(OnchainTx::getApplicationId, application.getId())
                .orderByDesc(OnchainTx::getCreatedAt)
                .last("limit 1");
        OnchainTx onchainTx = onchainTxMapper.selectOne(txWrapper);

        ApplicationStatusResponse response = new ApplicationStatusResponse();
        response.setApplicationNo(applicationNo);
        response.setSoftwareName(application.getSoftwareName());
        response.setStatus(application.getStatus());
        response.setSimilarityScore(application.getSimilarityScore());
        response.setRiskLevel(application.getRiskLevel());
        response.setRiskReason(application.getRiskReason());
        if (onchainTx != null) {
            response.setTxHash(onchainTx.getTxHash());
            response.setBlockNumber(onchainTx.getBlockNumber());
            response.setErrorMessage(onchainTx.getErrorMessage());
        }
        return response;
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
    public QueryResult queryByApplicationNo(String applicationNo) {
        LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CopyrightRecord::getApplicationNo, applicationNo);
        CopyrightRecord record = recordMapper.selectOne(wrapper);
        if (record != null) {
            return convertToQueryResult(record);
        }

        LambdaQueryWrapper<CopyrightApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(CopyrightApplication::getApplicationNo, applicationNo);
        CopyrightApplication application = applicationMapper.selectOne(appWrapper);
        if (application == null) {
            return null;
        }

        LambdaQueryWrapper<CopyrightEvidence> evidenceWrapper = new LambdaQueryWrapper<>();
        evidenceWrapper.eq(CopyrightEvidence::getApplicationId, application.getId()).last("limit 1");
        CopyrightEvidence evidence = evidenceMapper.selectOne(evidenceWrapper);

        QueryResult result = new QueryResult();
        result.setApplicationId(application.getId());
        result.setApplicationNo(application.getApplicationNo());
        result.setSoftwareName(application.getSoftwareName());
        result.setDescription(application.getDescription());
        result.setUserId(application.getUserId());
        result.setSubjectType(application.getSubjectType());
        result.setSubjectId(application.getSubjectId());
        result.setSubjectName(application.getSubjectName());
        result.setBizStatus(application.getStatus());
        result.setRiskLevel(application.getRiskLevel());
        result.setRiskReason(application.getRiskReason());
        result.setSimilarityScore(application.getSimilarityScore());
        result.setCreatedAt(application.getCreatedAt());
        result.setUpdatedAt(application.getUpdatedAt());
        if (evidence != null) {
            result.setFileHash(evidence.getFileHash());
            result.setMetadataHash(evidence.getMetadataHash());
            result.setEvidenceRootHash(evidence.getEvidenceRootHash());
            result.setNormalizedHash(evidence.getNormalizedHash());
            result.setSemanticHash(evidence.getSemanticHash());
        }
        return result;
    }

    @Override
    public Page<QueryResult> getMyRecords(Integer page, Integer size, String keyword, String bizStatus) {
        Long userId = authService.getCurrentUserId();
        User currentUser = authService.getCurrentUser();

        LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();
        if (currentUser != null
                && AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE.equals(currentUser.getAccountType())
                && AuthDomainRules.ENTERPRISE_ROLE_OWNER.equals(currentUser.getEnterpriseRole())) {
            wrapper.eq(CopyrightRecord::getSubjectType, AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE)
                    .eq(CopyrightRecord::getSubjectId, currentUser.getEnterpriseId());
        } else if (currentUser != null
                && AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE.equals(currentUser.getAccountType())
                && AuthDomainRules.ENTERPRISE_ROLE_LEGAL.equals(currentUser.getEnterpriseRole())
                && AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_ALL.equals(currentUser.getEnterpriseLegalScope())) {
            wrapper.eq(CopyrightRecord::getSubjectType, AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE)
                    .eq(CopyrightRecord::getSubjectId, currentUser.getEnterpriseId());
        } else {
            wrapper.eq(CopyrightRecord::getUserId, userId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CopyrightRecord::getSoftwareName, keyword)
                    .or()
                    .like(CopyrightRecord::getApplicationNo, keyword));
        }

        if (StringUtils.hasText(bizStatus)) {
            wrapper.eq(CopyrightRecord::getBizStatus, bizStatus);
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
    public Page<QueryResult> getEnterpriseRecords(Integer page, Integer size, String keyword, String bizStatus) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null
                || !AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE.equals(currentUser.getAccountType())
                || currentUser.getEnterpriseId() == null) {
            throw new RuntimeException("当前用户不属于企业主体");
        }
        if (AuthDomainRules.ENTERPRISE_ROLE_DEVELOPER.equals(currentUser.getEnterpriseRole())) {
            throw new RuntimeException("企业开发者仅可查看个人提交记录");
        }
        if (AuthDomainRules.ENTERPRISE_ROLE_LEGAL.equals(currentUser.getEnterpriseRole())
                && !AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_ALL.equals(currentUser.getEnterpriseLegalScope())) {
            throw new RuntimeException("当前法务账号未被授予全企业查看权限");
        }

        LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CopyrightRecord::getSubjectType, AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE)
                .eq(CopyrightRecord::getSubjectId, currentUser.getEnterpriseId());
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CopyrightRecord::getSoftwareName, keyword)
                    .or()
                    .like(CopyrightRecord::getApplicationNo, keyword)
                    .or()
                    .like(CopyrightRecord::getSubjectName, keyword));
        }
        if (StringUtils.hasText(bizStatus)) {
            wrapper.eq(CopyrightRecord::getBizStatus, bizStatus);
        }
        wrapper.orderByDesc(CopyrightRecord::getCreatedAt);
        Page<CopyrightRecord> recordPage = new Page<>(page, size);
        Page<CopyrightRecord> resultPage = recordMapper.selectPage(recordPage, wrapper);

        Page<QueryResult> queryResultPage = new Page<>(page, size, resultPage.getTotal());
        queryResultPage.setRecords(resultPage.getRecords().stream()
                .map(this::convertToQueryResult)
                .collect(Collectors.toList()));
        return queryResultPage;
    }

    @Override
    public Page<QueryResult> getAllCopyrights(Integer page, Integer size, String keyword, String bizStatus) {
        LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(CopyrightRecord::getSoftwareName, keyword)
                    .or()
                    .like(CopyrightRecord::getFileHash, keyword)
                    .or()
                    .like(CopyrightRecord::getApplicationNo, keyword);
        }

        if (StringUtils.hasText(bizStatus)) {
            wrapper.eq(CopyrightRecord::getBizStatus, bizStatus);
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
    public Page<QueryResult> getAuditRecords(Integer page, Integer size, String keyword, String auditStatus) {
        LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(CopyrightRecord::getSoftwareName, keyword)
                    .or()
                    .like(CopyrightRecord::getFileHash, keyword)
                    .or()
                    .like(CopyrightRecord::getSubjectName, keyword);
        }
        if (StringUtils.hasText(auditStatus)) {
            wrapper.eq(CopyrightRecord::getAuditStatus, auditStatus);
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
    public void auditRecord(Long recordId, String auditStatus, String auditComment) {
        if (!"APPROVED".equals(auditStatus) && !"REJECTED".equals(auditStatus)) {
            throw new RuntimeException("审核状态不合法");
        }
        CopyrightRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("版权记录不存在");
        }
        record.setAuditStatus(auditStatus);
        record.setAuditComment(auditComment);
        record.setAuditedBy(authService.getCurrentUserId());
        record.setAuditedAt(java.time.LocalDateTime.now());
        recordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewApplication(Long applicationId, String reviewResult, String reviewNote) {
        if (!"APPROVED".equals(reviewResult) && !"REJECTED".equals(reviewResult)) {
            throw new RuntimeException("复核结论不合法");
        }
        CopyrightApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }
        if (!"PENDING_REVIEW".equals(application.getStatus())) {
            throw new RuntimeException("当前申请不在待复核状态");
        }

        LambdaQueryWrapper<CopyrightEvidence> evidenceWrapper = new LambdaQueryWrapper<>();
        evidenceWrapper.eq(CopyrightEvidence::getApplicationId, application.getId()).last("limit 1");
        CopyrightEvidence evidence = evidenceMapper.selectOne(evidenceWrapper);
        if (evidence == null) {
            throw new RuntimeException("申请证据不存在");
        }

        if ("REJECTED".equals(reviewResult)) {
            // 人工驳回时只更新申请状态，不触发上链。
            application.setStatus("REJECTED");
            application.setRiskReason(reviewNote);
            applicationMapper.updateById(application);
            return;
        }

        Long userId = application.getUserId();
        String resolvedReviewNote = StringUtils.hasText(reviewNote) ? reviewNote : "人工复核通过";
        SimilarityDecision decision = new SimilarityDecision(
                application.getSimilarityScore() == null ? 0 : application.getSimilarityScore(),
                application.getRiskLevel(),
                application.getRiskReason(),
                false
        );
        processOnchain(
                application,
                evidence,
                toApplyRequest(application),
                null,
                userId,
                decision,
                reviewResult,
                resolvedReviewNote
        );
    }

    private String processOnchain(CopyrightApplication application,
                                  CopyrightEvidence evidence,
                                  ApplyCopyrightRequest request,
                                  User currentUser,
                                  Long userId,
                                  SimilarityDecision similarityDecision,
                                  String reviewResult,
                                  String reviewNote) {
        // 先记录一条 PENDING 交易行，保证上链异常时也能追溯到失败原因。
        OnchainTx onchainTx = new OnchainTx();
        onchainTx.setApplicationId(application.getId());
        onchainTx.setContractName("SoftwareEvidenceAnchor");
        onchainTx.setContractVersion("V2");
        onchainTx.setStatus("PENDING");
        onchainTxMapper.insert(onchainTx);

        log.info("=== 区块链交易准备 ===");
        log.info("配置的合约地址: {}", contractAddress);
        log.info("调用者地址: {}", cryptoKeyPair.getAddress());
        log.info("客户端群组: {}", client.getGroup());

        String normalizedAddress = contractAddress;
        if (!normalizedAddress.startsWith("0x") && normalizedAddress.length() > 20) {
            try {
                BigInteger bigInt = new BigInteger(normalizedAddress);
                normalizedAddress = "0x" + bigInt.toString(16);
                log.info("地址格式转换: {} -> {}", contractAddress, normalizedAddress);
            } catch (Exception e) {
                log.warn("地址转换失败,使用原始地址: {}", e.getMessage());
            }
        }

        SoftwareEvidenceAnchor contract = SoftwareEvidenceAnchor.load(normalizedAddress, client, cryptoKeyPair);
        log.info("加载后的合约对象地址: {}", contract.getContractAddress());
        log.info("证据根哈希: {}", evidence.getEvidenceRootHash());
        log.info("软件名称: {}", request.getSoftwareName());
        log.info("开始执行区块链交易, 合约地址: {}, 证据根哈希: {}", contractAddress, evidence.getEvidenceRootHash());

        try {
            // 上链前先查询是否已存在证据根，避免同证据重复注册导致业务语义冲突。
            byte[] hashBytes = FileHashUtils.hexStringToBytes(evidence.getEvidenceRootHash());
            var existingRecord = contract.queryEvidence(hashBytes);
            if (existingRecord != null && existingRecord.getValue5()) {
                String errorMsg = String.format(
                        "该证据根哈希已在区块链上注册 - 拥有者: %s, 时间戳: %d",
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

        TransactionReceipt receipt = contract.registerEvidence(
                FileHashUtils.hexStringToBytes(evidence.getEvidenceRootHash()),
                FileHashUtils.hexStringToBytes(evidence.getMetadataHash()),
                BigInteger.ONE
        );

        log.info("交易回执 - 状态码: {}, 消息: '{}', 交易哈希: {}, GasUsed: {}, 区块号: {}",
                receipt.getStatus(), receipt.getMessage(),
                receipt.getTransactionHash(), receipt.getGasUsed(),
                receipt.getBlockNumber());

        if (!receipt.isStatusOK()) {
            String errorMsg = String.format("区块链交易失败 - 状态码: %s, 交易哈希: %s",
                    receipt.getStatus(), receipt.getTransactionHash());
            log.error(errorMsg);
            onchainTx.setStatus("FAILED");
            onchainTx.setErrorMessage(errorMsg);
            onchainTxMapper.updateById(onchainTx);
            application.setStatus("ONCHAIN_FAILED");
            applicationMapper.updateById(application);
            throw new RuntimeException(errorMsg);
        }

        // 仅在交易成功后落正式版权记录，保持“链上状态”为业务真值来源。
        CopyrightRecord record = new CopyrightRecord();
        record.setFileHash(evidence.getFileHash());
        record.setSoftwareName(request.getSoftwareName());
        record.setDescription(request.getDescription());
        record.setOwnerAddress(receipt.getFrom());
        record.setTxHash(receipt.getTransactionHash());
        record.setBlockNumber(receipt.getBlockNumber().longValue());
        record.setEvidenceRootHash(evidence.getEvidenceRootHash());
        record.setMetadataHash(evidence.getMetadataHash());
        record.setNormalizedHash(evidence.getNormalizedHash());
        record.setSemanticHash(evidence.getSemanticHash());
        record.setSimilarityScore(similarityDecision.score());
        record.setRiskLevel(similarityDecision.riskLevel());
        record.setRiskReason(similarityDecision.riskReason());
        record.setApplicationId(application.getId());
        record.setApplicationNo(application.getApplicationNo());
        record.setUserId(userId != null ? userId : 0L);
        if (currentUser != null) {
            record.setSubjectType(currentUser.getAccountType());
            if (AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE.equals(currentUser.getAccountType())) {
                record.setSubjectId(currentUser.getEnterpriseId());
            } else {
                record.setSubjectId(currentUser.getId());
            }
            record.setSubjectName(currentUser.getDisplaySubjectName());
        } else {
            record.setSubjectType(application.getSubjectType());
            record.setSubjectId(application.getSubjectId());
            record.setSubjectName(application.getSubjectName());
        }
        record.setAuditStatus("PENDING");
        record.setBizStatus("ONCHAIN_SUCCESS");
        record.setReviewResult(reviewResult);
        record.setReviewNote(reviewNote);
        recordMapper.insert(record);

        onchainTx.setStatus("SUCCESS");
        onchainTx.setTxHash(receipt.getTransactionHash());
        onchainTx.setBlockNumber(receipt.getBlockNumber().longValue());
        onchainTx.setErrorMessage(null);
        onchainTxMapper.updateById(onchainTx);
        application.setStatus("ONCHAIN_SUCCESS");
        applicationMapper.updateById(application);
        return receipt.getTransactionHash();
    }

    private SimilarityDecision evaluateSimilarity(String semanticHash, User currentUser) {
        // 仅扫描最近样本，在可接受性能下完成风险筛查。
        LambdaQueryWrapper<CopyrightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(CopyrightRecord::getSemanticHash)
                .orderByDesc(CopyrightRecord::getCreatedAt)
                .last("limit " + SIMILARITY_SCAN_LIMIT);
        List<CopyrightRecord> candidates = recordMapper.selectList(wrapper);
        if (candidates == null || candidates.isEmpty()) {
            return new SimilarityDecision(0, "LOW", "未发现相似样本", false);
        }

        int maxScore = 0;
        CopyrightRecord matched = null;
        for (CopyrightRecord candidate : candidates) {
            int score = ArchiveFingerprintUtils.calculateSimilarityScore(semanticHash, candidate.getSemanticHash());
            if (score > maxScore) {
                maxScore = score;
                matched = candidate;
            }
        }
        if (matched == null) {
            return new SimilarityDecision(0, "LOW", "未发现相似样本", false);
        }

        boolean sameSubject = isSameSubject(currentUser, matched);
        if (maxScore >= HIGH_RISK_THRESHOLD && !sameSubject) {
            return new SimilarityDecision(maxScore, "HIGH",
                    "命中高相似跨主体样本 applicationNo=" + matched.getApplicationNo(),
                    true);
        }
        if (maxScore >= HIGH_RISK_THRESHOLD) {
            return new SimilarityDecision(maxScore, "MEDIUM",
                    "与同主体历史版本高度相似 applicationNo=" + matched.getApplicationNo(),
                    false);
        }
        if (maxScore >= MEDIUM_RISK_THRESHOLD) {
            return new SimilarityDecision(maxScore, "MEDIUM",
                    "命中中风险相似样本 applicationNo=" + matched.getApplicationNo(),
                    true);
        }
        return new SimilarityDecision(maxScore, "LOW",
                "最高相似度 " + maxScore + "%，低于人工复核阈值",
                false);
    }

    private boolean isSameSubject(User currentUser, CopyrightRecord record) {
        if (currentUser == null || record == null) {
            return false;
        }
        if (!StringUtils.hasText(record.getSubjectType()) || record.getSubjectId() == null) {
            return false;
        }
        if (!record.getSubjectType().equals(currentUser.getAccountType())) {
            return false;
        }
        Long currentSubjectId = AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE.equals(currentUser.getAccountType())
                ? currentUser.getEnterpriseId()
                : currentUser.getId();
        return currentSubjectId != null && currentSubjectId.equals(record.getSubjectId());
    }

    private ApplyCopyrightRequest toApplyRequest(CopyrightApplication application) {
        ApplyCopyrightRequest request = new ApplyCopyrightRequest();
        request.setSoftwareName(application.getSoftwareName());
        request.setDescription(application.getDescription());
        return request;
    }

    private record SimilarityDecision(int score, String riskLevel, String riskReason, boolean requireReview) {
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
        result.setEvidenceRootHash(record.getEvidenceRootHash());
        result.setMetadataHash(record.getMetadataHash());
        result.setNormalizedHash(record.getNormalizedHash());
        result.setSemanticHash(record.getSemanticHash());
        result.setSimilarityScore(record.getSimilarityScore());
        result.setRiskLevel(record.getRiskLevel());
        result.setRiskReason(record.getRiskReason());
        result.setCreatedAt(record.getCreatedAt());
        result.setUpdatedAt(record.getUpdatedAt());
        result.setUserId(record.getUserId());
        result.setApplicationId(record.getApplicationId());
        result.setApplicationNo(record.getApplicationNo());
        result.setSubjectType(record.getSubjectType());
        result.setSubjectId(record.getSubjectId());
        result.setSubjectName(record.getSubjectName());
        result.setAuditStatus(record.getAuditStatus());
        result.setBizStatus(record.getBizStatus());
        result.setAuditComment(record.getAuditComment());
        result.setReviewResult(record.getReviewResult());
        result.setReviewNote(record.getReviewNote());
        result.setSourceApplicationId(record.getSourceApplicationId());
        result.setAuditedBy(record.getAuditedBy());
        result.setAuditedAt(record.getAuditedAt());
        return result;
    }
}
