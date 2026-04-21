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

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
