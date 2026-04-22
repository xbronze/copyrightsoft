-- 版权存证主表
CREATE TABLE copyright_records (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   file_hash VARCHAR(66) UNIQUE NOT NULL COMMENT '文件哈希值',
                                   software_name VARCHAR(255) NOT NULL COMMENT '软件名称',
                                   owner_address VARCHAR(42) NOT NULL COMMENT '版权拥有者地址',
                                   description TEXT COMMENT '软件描述',
                                   tx_hash VARCHAR(66) COMMENT '区块链交易哈希',
                                   block_number BIGINT COMMENT '区块高度',
                                   user_id BIGINT DEFAULT 0 COMMENT '用户ID',
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE users (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
                      password VARCHAR(255) NOT NULL COMMENT '密码',
                      email VARCHAR(100) COMMENT '邮箱',
                      phone VARCHAR(20) COMMENT '手机号',
                      nickname VARCHAR(50) COMMENT '昵称',
                      role VARCHAR(20) DEFAULT 'USER' COMMENT '角色',
                      status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
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
