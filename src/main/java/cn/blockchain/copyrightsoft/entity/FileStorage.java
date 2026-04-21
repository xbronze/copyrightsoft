package cn.blockchain.copyrightsoft.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author: xbronze
 * @date: 2026-04-21 09:44
 * @description: TODO
 */
@Data
@TableName("file_storage")
public class FileStorage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalFilename;

    private String storedFilename;

    private Long fileSize;

    private String fileHash;

    private String storagePath;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
