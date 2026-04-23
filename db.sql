/*
 Navicat Premium Dump SQL

 Source Server         : unraid
 Source Server Type    : MariaDB
 Source Server Version : 110408 (11.4.8-MariaDB-log)
 Source Host           : 192.168.1.100:3356
 Source Schema         : soft_copyright

 Target Server Type    : MariaDB
 Target Server Version : 110408 (11.4.8-MariaDB-log)
 File Encoding         : 65001

 Date: 23/04/2026 19:02:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for copyright_records
-- ----------------------------
DROP TABLE IF EXISTS `copyright_records`;
CREATE TABLE `copyright_records`  (
                                      `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                      `file_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件哈希值',
                                      `software_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '软件名称',
                                      `owner_address` varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '版权拥有者地址',
                                      `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件描述',
                                      `tx_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区块链交易哈希',
                                      `block_number` bigint(20) NULL DEFAULT NULL COMMENT '区块高度',
                                      `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
                                      `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
                                      `created_at` timestamp NULL DEFAULT current_timestamp(),
                                      `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
                                      `subject_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归属主体类型',
                                      `subject_id` bigint(20) NULL DEFAULT NULL COMMENT '归属主体ID',
                                      `subject_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归属主体名称',
                                      `audit_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
                                      `audit_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
                                      `audited_by` bigint(20) NULL DEFAULT NULL COMMENT '审核人ID',
                                      `audited_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `file_hash`(`file_hash` ASC) USING BTREE,
                                      INDEX `idx_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of copyright_records
-- ----------------------------
INSERT INTO `copyright_records` VALUES (1, '204e8f3f5a9c436556d4a710c318e1ec38877fe1eb0725ff33ea0f181507fb16', '20260416听课记录表', '0xa0670059b8ef4565f61237966313bd7ff8a5abde', '这是听课记录表416', '0xaabdc65b0bebe685c0fa495730b1e389ba0cfd3f7d2f37b6b0db34d60d604bf1', 15, 1, 0, '2026-04-21 19:36:09', '2026-04-22 20:15:06', NULL, NULL, NULL, 'PENDING', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for enterprise
-- ----------------------------
DROP TABLE IF EXISTS `enterprise`;
CREATE TABLE `enterprise`  (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT,
                               `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业名称',
                               `license_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业证照号',
                               `status` int(11) NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
                               `deleted` int(11) NULL DEFAULT 0,
                               `created_at` timestamp NULL DEFAULT current_timestamp(),
                               `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enterprise
-- ----------------------------
INSERT INTO `enterprise` VALUES (1, '世纪鼎利', '', 1, 0, '2026-04-23 18:54:05', '2026-04-23 18:54:05');
INSERT INTO `enterprise` VALUES (2, '世纪鼎利', '', 1, 0, '2026-04-23 18:57:47', '2026-04-23 18:57:47');

-- ----------------------------
-- Table structure for file_storage
-- ----------------------------
DROP TABLE IF EXISTS `file_storage`;
CREATE TABLE `file_storage`  (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                 `stored_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                 `file_size` bigint(20) NULL DEFAULT NULL,
                                 `file_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                 `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                 `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
                                 `created_at` timestamp NULL DEFAULT current_timestamp(),
                                 PRIMARY KEY (`id`) USING BTREE,
                                 INDEX `idx_deleted_file`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_storage
-- ----------------------------
INSERT INTO `file_storage` VALUES (13, '2026-04-16-鼎利教育听课记录表.pdf', 'b6c18e7f1dea498997e05fe0384a61e7.pdf', 506082, '204e8f3f5a9c436556d4a710c318e1ec38877fe1eb0725ff33ea0f181507fb16', 'copyright-files/b6c18e7f1dea498997e05fe0384a61e7.pdf', 0, '2026-04-21 19:36:08');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
                          `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                          `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
                          `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
                          `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
                          `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'USER' COMMENT '角色',
                          `status` int(11) NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
                          `created_at` timestamp NULL DEFAULT current_timestamp(),
                          `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
                          `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
                          `account_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主体类型：INDIVIDUAL/ENTERPRISE',
                          `enterprise_id` bigint(20) NULL DEFAULT NULL COMMENT '企业ID（企业账户使用）',
                          `display_subject_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '显示主体名称（个人昵称或企业名）',
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'xuzh', '$2a$10$K2BgeKi1wq.ppTviSPpdfuwIZ.hTlfMOHmx/xlFiqeoeJLIlPxabu', 'zihui_xu@126.com', '13462689755', '天使', 'INDIVIDUAL_DEVELOPER', 1, '2026-04-22 18:04:42', '2026-04-23 18:18:39', 0, 'INDIVIDUAL', NULL, '天使');
INSERT INTO `users` VALUES (3, 'admin', '$2a$10$nknCZHuK1NdjKaDHIo//FuIb8CBD7Bukqd/VuKiCpdxAt7R.qj3nW', 'admin@copyrightsoft.com', '', '系统管理员', 'ADMIN', 1, '2026-04-23 11:50:48', '2026-04-23 18:18:39', 0, 'INDIVIDUAL', NULL, '系统管理员');
INSERT INTO `users` VALUES (6, 'dingli', '$2a$10$hFx.k1DKIp5Zxr.APHAoW.87fLHO43pvXf2DFgQti.gIFLpsqX2a6', 'dingli@copyrightsoft.com', '', '', 'ENTERPRISE_DEVELOPER', 1, '2026-04-23 18:57:47', '2026-04-23 18:57:47', 0, 'ENTERPRISE', 2, '世纪鼎利');
INSERT INTO `users` VALUES (7, 'shenhe', '$2a$10$XuZW7YpkNbiSd7HwSHSgIOPLZGRPUufLeSDqm.P.uVS8MTXf2Cxwm', 'shenhe@copyrightsoft.com', '', '审核', 'AUDITOR', 1, '2026-04-23 18:58:25', '2026-04-23 18:58:25', 0, 'INDIVIDUAL', NULL, '审核');

SET FOREIGN_KEY_CHECKS = 1;
