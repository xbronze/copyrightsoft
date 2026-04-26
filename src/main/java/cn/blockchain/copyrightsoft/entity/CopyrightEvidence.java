package cn.blockchain.copyrightsoft.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("copyright_evidence")
public class CopyrightEvidence {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private Long fileStorageId;

    private String fileHash;

    private String metadataHash;

    private String evidenceRootHash;

    private String normalizedHash;

    private String semanticHash;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
