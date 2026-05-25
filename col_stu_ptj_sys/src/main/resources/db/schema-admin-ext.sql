-- 管理员扩展：账号启停、信用、系统配置、争议工单
-- 在已有库 col_stu_ptj_sys 中执行。
-- 若 `account_enabled` 已存在，请删除或注释下面 ALTER 段，避免 Duplicate column 错误。
-- 建议顺序：先 schema-job.sql（岗位表），再本脚本。

-- 1) 用户表：账号启用状态
ALTER TABLE `user`
  ADD COLUMN `account_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用' AFTER `auth_time`;

-- 2) 用户信用档案（每人一条，与 user.id 关联）
CREATE TABLE IF NOT EXISTS `user_credit` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `score` int NOT NULL DEFAULT 100 COMMENT '信用分 0-1000',
  `credit_level` varchar(32) NOT NULL DEFAULT 'NORMAL' COMMENT '等级标识',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_credit_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信用档案';

-- 3) 信用人工调整记录
CREATE TABLE IF NOT EXISTS `credit_adjust_log` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `delta` int NOT NULL COMMENT '分数变化',
  `reason` varchar(500) NOT NULL,
  `admin_username` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_credit_adjust_user` (`user_id`),
  KEY `idx_credit_adjust_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='信用人工调整日志';

-- 4) 信用争议工单
CREATE TABLE IF NOT EXISTS `credit_dispute` (
  `id` bigint NOT NULL,
  `reporter_user_id` bigint NOT NULL,
  `target_user_id` bigint NOT NULL,
  `related_type` varchar(32) DEFAULT NULL,
  `related_id` bigint DEFAULT NULL,
  `reason` varchar(1000) NOT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'PENDING',
  `admin_remark` varchar(1000) DEFAULT NULL,
  `resolved_at` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_dispute_status` (`status`),
  KEY `idx_dispute_target` (`target_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='信用争议';

-- 5) 系统配置（公告、轮播图JSON、规则说明等）
CREATE TABLE IF NOT EXISTS `sys_config` (
  `config_key` varchar(64) NOT NULL,
  `config_value` text,
  `remark` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置';

INSERT INTO `sys_config` (`config_key`, `config_value`, `remark`, `update_time`) VALUES
('announcement', '', '平台公告（可 HTML/纯文本）', NOW()),
('banner_images', '[]', '首页轮播图 URL 列表 JSON 数组', NOW()),
('credit_rules', '信用分初始100；良好行为加分，违约扣分；管理员可人工复核。', '信用规则说明', NOW())
ON DUPLICATE KEY UPDATE `config_key` = `config_key`;
