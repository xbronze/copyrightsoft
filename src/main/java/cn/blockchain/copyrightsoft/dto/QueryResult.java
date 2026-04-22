package cn.blockchain.copyrightsoft.dto;

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
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
