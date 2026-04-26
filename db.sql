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

 Date: 26/04/2026 20:01:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for copyright_application
-- ----------------------------
DROP TABLE IF EXISTS `copyright_application`;
CREATE TABLE `copyright_application`  (
                                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                          `application_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请编号',
                                          `software_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '软件名称',
                                          `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件描述',
                                          `user_id` bigint(20) NOT NULL COMMENT '申请用户ID',
                                          `subject_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归属主体类型',
                                          `subject_id` bigint(20) NULL DEFAULT NULL COMMENT '归属主体ID',
                                          `subject_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归属主体名称',
                                          `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：SUBMITTED/ONCHAIN_SUCCESS/ONCHAIN_FAILED',
                                          `risk_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'LOW' COMMENT '风险等级:LOW/MEDIUM/HIGH',
                                          `risk_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风险判定原因',
                                          `similarity_score` int(11) NULL DEFAULT NULL COMMENT '相似度评分(0-100)',
                                          `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
                                          `created_at` timestamp NULL DEFAULT current_timestamp(),
                                          `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_application_no`(`application_no` ASC) USING BTREE,
                                          INDEX `idx_app_user`(`user_id` ASC) USING BTREE,
                                          INDEX `idx_app_status_time`(`status` ASC, `created_at` ASC) USING BTREE,
                                          INDEX `idx_app_risk_level`(`risk_level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of copyright_application
-- ----------------------------
INSERT INTO `copyright_application` VALUES (1, 'APP-1777118256328-65AA9588', 'jakarta.servlet-api-6.1.0', 'jakarta.servlet-api-6.1.0', 6, 'ENTERPRISE', 2, '世纪鼎利', 'ONCHAIN_SUCCESS', 'LOW', NULL, NULL, 0, '2026-04-25 19:57:36', '2026-04-25 19:57:36');

-- ----------------------------
-- Table structure for copyright_evidence
-- ----------------------------
DROP TABLE IF EXISTS `copyright_evidence`;
CREATE TABLE `copyright_evidence`  (
                                       `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                       `application_id` bigint(20) NOT NULL COMMENT '申请ID',
                                       `file_storage_id` bigint(20) NOT NULL COMMENT '文件存储ID',
                                       `file_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件哈希',
                                       `metadata_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '元数据哈希',
                                       `evidence_root_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '证据根哈希',
                                       `normalized_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规范化压缩包哈希',
                                       `semantic_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '语义哈希(去注释和空白)',
                                       `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
                                       `created_at` timestamp NULL DEFAULT current_timestamp(),
                                       `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`id`) USING BTREE,
                                       INDEX `idx_evi_app`(`application_id` ASC) USING BTREE,
                                       INDEX `idx_evi_hash`(`file_hash` ASC) USING BTREE,
                                       INDEX `idx_evi_normalized_hash`(`normalized_hash` ASC) USING BTREE,
                                       INDEX `idx_evi_semantic_hash`(`semantic_hash` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of copyright_evidence
-- ----------------------------
INSERT INTO `copyright_evidence` VALUES (1, 1, 14, '8a31f465f3593bf2351531a5c952014eb839da96a605b5825b93dd54714c48c4', 'ffd10d0206a26e37fc9632220587eba15bd596413ef88720d0ba82cdbf2b2fd6', '707d269a6421cd4f556db550925576bcfe6213041915c7d5b468215d4bafe22b', NULL, NULL, 0, '2026-04-25 19:57:36', '2026-04-25 19:57:36');

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
                                      `evidence_root_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证据根哈希',
                                      `metadata_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '元数据哈希',
                                      `normalized_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规范化压缩包哈希',
                                      `semantic_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '语义哈希(去注释和空白)',
                                      `similarity_score` int(11) NULL DEFAULT NULL COMMENT '相似度评分(0-100)',
                                      `risk_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'LOW' COMMENT '风险等级:LOW/MEDIUM/HIGH',
                                      `risk_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风险判定原因',
                                      `review_result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '复核结论',
                                      `review_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '复核备注',
                                      `source_application_id` bigint(20) NULL DEFAULT NULL COMMENT '源申请ID(版本谱系)',
                                      `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
                                      `application_id` bigint(20) NULL DEFAULT NULL COMMENT '申请ID',
                                      `application_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请编号',
                                      `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
                                      `created_at` timestamp NULL DEFAULT current_timestamp(),
                                      `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
                                      `subject_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归属主体类型',
                                      `subject_id` bigint(20) NULL DEFAULT NULL COMMENT '归属主体ID',
                                      `subject_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归属主体名称',
                                      `audit_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
                                      `biz_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ONCHAIN_SUCCESS' COMMENT '业务状态',
                                      `audit_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
                                      `audited_by` bigint(20) NULL DEFAULT NULL COMMENT '审核人ID',
                                      `audited_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `file_hash`(`file_hash` ASC) USING BTREE,
                                      INDEX `idx_deleted`(`deleted` ASC) USING BTREE,
                                      INDEX `idx_records_normalized_hash`(`normalized_hash` ASC) USING BTREE,
                                      INDEX `idx_records_semantic_hash`(`semantic_hash` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of copyright_records
-- ----------------------------
INSERT INTO `copyright_records` VALUES (1, '204e8f3f5a9c436556d4a710c318e1ec38877fe1eb0725ff33ea0f181507fb16', '20260416听课记录表', '0xa0670059b8ef4565f61237966313bd7ff8a5abde', '这是听课记录表416', '0xaabdc65b0bebe685c0fa495730b1e389ba0cfd3f7d2f37b6b0db34d60d604bf1', 15, NULL, NULL, NULL, NULL, NULL, 'LOW', NULL, NULL, NULL, NULL, 1, NULL, NULL, 0, '2026-04-21 19:36:09', '2026-04-22 20:15:06', NULL, NULL, NULL, 'REJECTED', 'ONCHAIN_SUCCESS', '不同意', 3, '2026-04-26 17:33:57');
INSERT INTO `copyright_records` VALUES (2, '8a31f465f3593bf2351531a5c952014eb839da96a605b5825b93dd54714c48c4', 'jakarta.servlet-api-6.1.0', '0xc4c2b12764168935fe00deaf21980261d5677e12', 'jakarta.servlet-api-6.1.0', '0xc395824f29edb5e7ad0323f568185436fe3007c0d7816abb0ed4ea5238254222', 17, '707d269a6421cd4f556db550925576bcfe6213041915c7d5b468215d4bafe22b', 'ffd10d0206a26e37fc9632220587eba15bd596413ef88720d0ba82cdbf2b2fd6', NULL, NULL, NULL, 'LOW', NULL, NULL, NULL, NULL, 6, 1, 'APP-1777118256328-65AA9588', 0, '2026-04-25 19:57:37', '2026-04-25 19:57:37', 'ENTERPRISE', 2, '世纪鼎利', 'APPROVED', 'ONCHAIN_SUCCESS', '同意认证版本', 3, '2026-04-25 20:05:10');

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enterprise
-- ----------------------------
INSERT INTO `enterprise` VALUES (1, '世纪鼎利', '', 1, 0, '2026-04-23 18:54:05', '2026-04-23 18:54:05');
INSERT INTO `enterprise` VALUES (3, '世纪鼎利', '', 1, 0, '2026-04-26 18:20:41', '2026-04-26 18:20:41');

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
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_storage
-- ----------------------------
INSERT INTO `file_storage` VALUES (13, '2026-04-16-鼎利教育听课记录表.pdf', 'b6c18e7f1dea498997e05fe0384a61e7.pdf', 506082, '204e8f3f5a9c436556d4a710c318e1ec38877fe1eb0725ff33ea0f181507fb16', 'copyright-files/b6c18e7f1dea498997e05fe0384a61e7.pdf', 0, '2026-04-21 19:36:08');
INSERT INTO `file_storage` VALUES (14, 'jakarta.servlet-api-6.1.0.jar', '139238d3dfd748c189d27eae6cbca280.jar', 398424, '8a31f465f3593bf2351531a5c952014eb839da96a605b5825b93dd54714c48c4', 'copyright-files/139238d3dfd748c189d27eae6cbca280.jar', 0, '2026-04-25 19:57:36');

-- ----------------------------
-- Table structure for onchain_tx
-- ----------------------------
DROP TABLE IF EXISTS `onchain_tx`;
CREATE TABLE `onchain_tx`  (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT,
                               `application_id` bigint(20) NOT NULL COMMENT '申请ID',
                               `contract_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合约名称',
                               `contract_version` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'V2' COMMENT '合约版本',
                               `tx_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易哈希',
                               `block_number` bigint(20) NULL DEFAULT NULL COMMENT '区块高度',
                               `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/SUCCESS/FAILED',
                               `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败信息',
                               `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
                               `created_at` timestamp NULL DEFAULT current_timestamp(),
                               `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `idx_tx_app`(`application_id` ASC) USING BTREE,
                               INDEX `idx_tx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of onchain_tx
-- ----------------------------
INSERT INTO `onchain_tx` VALUES (1, 1, 'SoftwareEvidenceAnchor', 'V2', '0xc395824f29edb5e7ad0323f568185436fe3007c0d7816abb0ed4ea5238254222', 17, 'SUCCESS', NULL, 0, '2026-04-25 19:57:36', '2026-04-25 19:57:36');

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
                          `enterprise_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业内角色：OWNER/DEVELOPER/LEGAL',
                          `enterprise_legal_scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SELF' COMMENT '企业法务可见范围：SELF/ALL',
                          `display_subject_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '显示主体名称（个人昵称或企业名）',
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `username`(`username` ASC) USING BTREE,
                          INDEX `idx_users_enterprise_role`(`enterprise_id` ASC, `enterprise_role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'xuzh', '$2a$10$K2BgeKi1wq.ppTviSPpdfuwIZ.hTlfMOHmx/xlFiqeoeJLIlPxabu', 'zihui_xu@126.com', '13462689755', '天使', 'INDIVIDUAL_DEVELOPER', 1, '2026-04-22 18:04:42', '2026-04-23 18:18:39', 0, 'INDIVIDUAL', NULL, NULL, 'SELF', '天使');
INSERT INTO `users` VALUES (3, 'admin', '$2a$10$nknCZHuK1NdjKaDHIo//FuIb8CBD7Bukqd/VuKiCpdxAt7R.qj3nW', 'admin@copyrightsoft.com', '', '系统管理员', 'ADMIN', 1, '2026-04-23 11:50:48', '2026-04-23 18:18:39', 0, 'INDIVIDUAL', NULL, NULL, 'SELF', '系统管理员');
INSERT INTO `users` VALUES (6, 'dingli', '$2a$10$hFx.k1DKIp5Zxr.APHAoW.87fLHO43pvXf2DFgQti.gIFLpsqX2a6', 'dingli@copyrightsoft.com', '', '', 'ENTERPRISE_DEVELOPER', 1, '2026-04-23 18:57:47', '2026-04-26 19:24:14', 0, 'ENTERPRISE', 2, 'OWNER', 'ALL', '世纪鼎利');
INSERT INTO `users` VALUES (7, 'shenhe', '$2a$10$XuZW7YpkNbiSd7HwSHSgIOPLZGRPUufLeSDqm.P.uVS8MTXf2Cxwm', 'shenhe@copyrightsoft.com', '', '审核', 'AUDITOR', 1, '2026-04-23 18:58:25', '2026-04-23 18:58:25', 0, 'INDIVIDUAL', NULL, NULL, 'SELF', '审核');
INSERT INTO `users` VALUES (9, 'dingli1', '$2a$10$SQtmM/j63R4qO9GTxPeooOtkQwHZ4xjsKduyahtC2UxBFUvjYhR7a', '', '', '鼎利员工1', 'ENTERPRISE_DEVELOPER', 1, '2026-04-26 19:28:49', '2026-04-26 19:28:49', 0, 'ENTERPRISE', 2, 'DEVELOPER', 'SELF', '世纪鼎利');
INSERT INTO `users` VALUES (10, 'dingli3', '$2a$10$QxL1mufblH7H/dNKM1AhweMgTDrjOYO.wGYopp3w0Hw93NP.JCR9G', '', '', '鼎利法务3', 'ENTERPRISE_LEGAL', 1, '2026-04-26 19:29:33', '2026-04-26 19:29:33', 0, 'ENTERPRISE', 2, 'LEGAL', 'ALL', '世纪鼎利');

SET FOREIGN_KEY_CHECKS = 1;
