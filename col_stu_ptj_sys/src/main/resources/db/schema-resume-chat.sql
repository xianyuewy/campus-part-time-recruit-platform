-- 学生简历 + 投递在线沟通（执行一次即可）
CREATE TABLE IF NOT EXISTS `student_resume` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL COMMENT '学生用户ID',
  `self_intro` TEXT COMMENT '自我介绍',
  `education` TEXT COMMENT '教育经历',
  `skills` TEXT COMMENT '技能特长',
  `work_experience` TEXT COMMENT '工作/实践经历',
  `attachment_path` VARCHAR(512) DEFAULT NULL COMMENT '简历PDF等附件URL',
  `attachment_original_name` VARCHAR(260) DEFAULT NULL COMMENT '附件原始文件名',
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resume_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `application_message` (
  `id` BIGINT NOT NULL,
  `application_id` BIGINT NOT NULL COMMENT '投递ID',
  `sender_user_id` BIGINT NOT NULL COMMENT '发送方用户ID',
  `content` VARCHAR(2000) NOT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_app_msg_application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
