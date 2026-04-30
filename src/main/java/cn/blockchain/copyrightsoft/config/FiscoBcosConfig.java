package cn.blockchain.copyrightsoft.config;



import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Data
@Configuration
/**
 * FISCO BCOS 客户端配置。
 * <p>
 * 运行时将 classpath 下的链配置文件提取到临时目录，并动态修正 config.toml 中的证书路径，
 * 避免不同运行目录导致 SDK 无法读取证书的问题。
 */
public class FiscoBcosConfig {

    @Value("${fisco.bcos.cert-path}")
    private String certPath;

    @Value("${fisco.bcos.group-id}")
    private String groupId;

    @Value("${fisco.bcos.contract-address:}")
    private String contractAddress;

    @Bean
    public BcosSDK bcosSDK() throws Exception {
        String configFilePath = extractConfigFiles();
        log.info("FISCO BCOS 配置文件路径: {}", configFilePath);

        try {
            return BcosSDK.build(configFilePath);
        } catch (Exception e) {
            log.error("初始化 FISCO BCOS SDK 失败,配置文件路径: {}", configFilePath, e);
            throw e;
        }
    }

    @Bean
    public Client client(BcosSDK bcosSDK) {
        return bcosSDK.getClient(groupId);
    }

    @Bean
    public CryptoKeyPair cryptoKeyPair(Client client) {
        return client.getCryptoSuite().getCryptoKeyPair();
    }

//    @Bean
//    public String contractAddress(@Value("${fisco.bcos.contract-address}") String address) {
//        // 确保地址格式正确
//        if (address != null && !address.startsWith("0x")) {
//            // 如果是十进制,转换为十六进制
//            try {
//                java.math.BigInteger bigInt = new java.math.BigInteger(address);
//                String hexAddress = "0x" + bigInt.toString(16);
//                log.info("合约地址从十进制转换为十六进制: {} -> {}", address, hexAddress);
//                return hexAddress;
//            } catch (Exception e) {
//                log.error("合约地址格式转换失败: {}", address, e);
//            }
//        }
//        log.info("使用合约地址: {}", address);
//        return address;
//    }

    private String extractConfigFiles() throws Exception {
        String[] configFiles = {"config.toml", "ca.crt", "sdk.crt", "sdk.key", "sdk.nodeid"};

        Path tempDir = Files.createTempDirectory("fisco-config-");
        tempDir.toFile().deleteOnExit();

        String configTomlPath = null;
        String tempDirPath = tempDir.toString().replace("\\", "/");

        for (String fileName : configFiles) {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("conf/" + fileName);

            if (inputStream != null) {
                File outputFile = tempDir.resolve(fileName).toFile();

                if ("config.toml".equals(fileName)) {
                    // config.toml 内 certPath/keyStoreDir 必须指向当前进程可访问的绝对路径。
                    String content = new String(inputStream.readAllBytes());

                    content = content.replaceAll("certPath\\s*=\\s*\"[^\"]*\"", "certPath = \"" + tempDirPath + "\"");
                    content = content.replaceAll("keyStoreDir\\s*=\\s*\"[^\"]*\"", "keyStoreDir = \"" + tempDirPath + "\"");

                    try (FileWriter writer = new FileWriter(outputFile)) {
                        writer.write(content);
                    }

                    configTomlPath = outputFile.getAbsolutePath();
                    log.info("config.toml 已提取并修改路径到: {}", configTomlPath);
                    log.debug("修改后的 certPath 和 keyStoreDir: {}", tempDirPath);
                } else {
                    try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    log.debug("提取配置文件: {}", outputFile.getAbsolutePath());
                }

                inputStream.close();
            } else {
                log.warn("未找到配置文件: conf/{}", fileName);
            }
        }

        if (configTomlPath == null) {
            throw new RuntimeException("无法找到 config.toml 文件");
        }

        log.info("所有配置文件提取完成,目录: {}", tempDirPath);
        File configDir = tempDir.toFile();
        File[] files = configDir.listFiles();
        if (files != null) {
            for (File f : files) {
                log.info("  - {} ({} bytes)", f.getName(), f.length());
            }
        }

        return configTomlPath;
    }
}
