package cn.blockchain.copyrightsoft.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author: xbronze
 * @date: 2026-04-21 09:40
 * @description: TODO
 */
@Data
@TableName("copyright_records")
public class CopyrightRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileHash;

    private String softwareName;

    private String ownerAddress;

    private String description;

    private String txHash;

    private Long blockNumber;

    private String evidenceRootHash;

    private String metadataHash;

    private String normalizedHash;

    private String semanticHash;

    private Integer similarityScore;

    private String riskLevel;

    private String riskReason;

    // 添加用户ID字段
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

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
