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
    private String subjectType;
    private Long subjectId;
    private String subjectName;
    private String auditStatus;
    private String auditComment;
    private Long auditedBy;
    private LocalDateTime auditedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
