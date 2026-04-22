package cn.blockchain.copyrightsoft.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QueryResult {
    private Long id;
    private String fileHash;
    private String softwareName;
    private String description;
    private String ownerAddress;
    private String txHash;
    private Long blockNumber;
    private Long timestamp;
    private Long fileSize;
    private String fileType;
    // 添加用户ID字段
    private Long userId;
    private LocalDateTime createTime;
}
