-- 兼职岗位表（MySQL 8+）。执行前请确认数据库 `col_stu_ptj_sys` 已创建。
-- 与实体 org.example.col_stu_ptj_sys.entity.Job 对齐。

CREATE TABLE IF NOT EXISTS `job` (
  `id` bigint NOT NULL COMMENT '主键',
  `publisher_user_id` bigint NOT NULL COMMENT '发布者用户ID（企业账号）',
  `title` varchar(200) NOT NULL COMMENT '岗位标题',
  `description` text COMMENT '岗位描述',
  `location` varchar(200) DEFAULT NULL COMMENT '工作地点',
  `job_type` varchar(100) DEFAULT NULL COMMENT '岗位类型',
  `salary_text` varchar(100) DEFAULT NULL COMMENT '薪资展示文案',
  `salary_min` int DEFAULT NULL COMMENT '最低薪资(元)',
  `salary_max` int DEFAULT NULL COMMENT '最高薪资(元)',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT '岗位联系电话',
  `contact_wechat` varchar(500) DEFAULT NULL COMMENT '微信/沟通说明',
  `expire_at` datetime DEFAULT NULL COMMENT '岗位过期时间',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/OFFLINE',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_job_status` (`status`),
  KEY `idx_job_publisher` (`publisher_user_id`),
  KEY `idx_job_job_type` (`job_type`),
  KEY `idx_job_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='兼职岗位';

CREATE TABLE IF NOT EXISTS `company_profile` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '企业用户ID',
  `company_name` varchar(200) NOT NULL COMMENT '企业名称',
  `license_no` varchar(120) DEFAULT NULL COMMENT '营业执照号',
  `contact_name` varchar(100) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `description` text COMMENT '企业介绍',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_profile_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业资料';

CREATE TABLE IF NOT EXISTS `job_application` (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `student_user_id` bigint NOT NULL COMMENT '学生用户ID',
  `company_user_id` bigint NOT NULL COMMENT '企业用户ID',
  `status` varchar(32) NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/VIEWED/INTERVIEW/HIRED/REJECTED/CANCELLED',
  `intention` varchar(500) DEFAULT NULL COMMENT '投递意向/备注',
  `interview_time` datetime DEFAULT NULL COMMENT '面试时间',
  `interview_location` varchar(200) DEFAULT NULL COMMENT '面试地点',
  `interview_note` varchar(500) DEFAULT NULL COMMENT '面试说明',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_student_unique` (`job_id`, `student_user_id`),
  KEY `idx_application_student` (`student_user_id`),
  KEY `idx_application_company` (`company_user_id`),
  KEY `idx_application_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位申请';

CREATE TABLE IF NOT EXISTS `job_favorite` (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `student_user_id` bigint NOT NULL COMMENT '学生用户ID',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_student_job` (`job_id`, `student_user_id`),
  KEY `idx_favorite_student` (`student_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位收藏';

CREATE TABLE IF NOT EXISTS `job_review` (
  `id` bigint NOT NULL COMMENT '主键',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `from_user_id` bigint NOT NULL COMMENT '评价发起方',
  `to_user_id` bigint NOT NULL COMMENT '被评价方',
  `score` int NOT NULL COMMENT '评分 1-5',
  `content` varchar(1000) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_review_to_user` (`to_user_id`),
  KEY `idx_review_application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='兼职评价';
