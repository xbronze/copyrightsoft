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
    private String evidenceRootHash;
    private String metadataHash;
    private String normalizedHash;
    private String semanticHash;
    private Integer similarityScore;
    private String riskLevel;
    private String riskReason;
    private Long userId;
    private Long applicationId;
    private String applicationNo;
    private String subjectType;
    private Long subjectId;
    private String subjectName;
    private String auditStatus;
    private String bizStatus;
    private String auditComment;
    private String reviewResult;
    private String reviewNote;
    private Long sourceApplicationId;
    private Long auditedBy;
    private LocalDateTime auditedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
