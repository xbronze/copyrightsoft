-- 版权存证主表
CREATE TABLE copyright_records (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   file_hash VARCHAR(66) UNIQUE NOT NULL COMMENT '文件哈希值',
                                   software_name VARCHAR(255) NOT NULL COMMENT '软件名称',
                                   owner_address VARCHAR(42) NOT NULL COMMENT '版权拥有者地址',
                                   description TEXT COMMENT '软件描述',
                                   tx_hash VARCHAR(66) COMMENT '区块链交易哈希',
                                   block_number BIGINT COMMENT '区块高度',
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 文件存储记录表
CREATE TABLE file_storage (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              original_filename VARCHAR(255) NOT NULL,
                              stored_filename VARCHAR(255) NOT NULL,
                              file_size BIGINT,
                              file_hash VARCHAR(66) NOT NULL,
                              storage_path VARCHAR(500),
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);