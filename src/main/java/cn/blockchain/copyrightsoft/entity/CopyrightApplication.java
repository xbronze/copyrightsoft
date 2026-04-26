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
@TableName("copyright_application")
public class CopyrightApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applicationNo;

    private String softwareName;

    private String description;

    private Long userId;

    private String subjectType;

    private Long subjectId;

    private String subjectName;

    private String status;

    private String riskLevel;

    private String riskReason;

    private Integer similarityScore;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
