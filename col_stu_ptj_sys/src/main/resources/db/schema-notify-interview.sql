-- 通知中心 + 面试安排 + 岗位有效期 扩展（执行一次即可）

CREATE TABLE IF NOT EXISTS `system_notification` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `biz_type` VARCHAR(64) DEFAULT NULL,
  `biz_id` VARCHAR(64) DEFAULT NULL,
  `read_flag` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_notify_user_read` (`user_id`, `read_flag`),
  KEY `idx_notify_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `job`
  ADD COLUMN `expire_at` DATETIME NULL COMMENT '岗位过期时间';

ALTER TABLE `job_application`
  ADD COLUMN `interview_time` DATETIME NULL COMMENT '面试时间',
  ADD COLUMN `interview_location` VARCHAR(200) NULL COMMENT '面试地点',
  ADD COLUMN `interview_note` VARCHAR(500) NULL COMMENT '面试说明';
