/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : col_stu_ptj_sys

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 16/06/2026 12:58:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for application_message
-- ----------------------------
DROP TABLE IF EXISTS `application_message`;
CREATE TABLE `application_message`  (
  `id` bigint NOT NULL,
  `application_id` bigint NOT NULL COMMENT '投递ID',
  `sender_user_id` bigint NOT NULL COMMENT '发送方用户ID',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_app_msg_application`(`application_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of application_message
-- ----------------------------
INSERT INTO `application_message` VALUES (2046767985986682882, 2044337540137299970, 2044318541303296001, '你好', '2026-04-22 09:48:10', '2026-04-22 09:48:10', 0);
INSERT INTO `application_message` VALUES (2046771086323929089, 2044337540137299970, 2044318541303296001, '你好', '2026-04-22 10:00:29', '2026-04-22 10:00:29', 0);
INSERT INTO `application_message` VALUES (2046771173775167490, 2044337540137299970, 2044320973630590977, '?', '2026-04-22 10:00:50', '2026-04-22 10:00:50', 0);
INSERT INTO `application_message` VALUES (2046771258663686145, 2044337540137299970, 2044318541303296001, '1', '2026-04-22 10:01:10', '2026-04-22 10:01:10', 0);
INSERT INTO `application_message` VALUES (2046804227214307330, 2044337540137299970, 2044320973630590977, '测试收件箱功能', '2026-04-22 12:12:10', '2026-04-22 12:12:10', 0);
INSERT INTO `application_message` VALUES (2046984640251686914, 2046804401919651841, 2044318541303296001, '111', '2026-04-23 00:09:04', '2026-04-23 00:09:04', 0);
INSERT INTO `application_message` VALUES (2048254739054936066, 2046804401919651841, 2044320973630590977, '1', '2026-04-26 12:15:59', '2026-04-26 12:15:59', 0);
INSERT INTO `application_message` VALUES (2048626755466526722, 2048626654853562370, 2048626318243889154, '你好', '2026-04-27 12:54:15', '2026-04-27 12:54:15', 0);
INSERT INTO `application_message` VALUES (2048628300778143746, 2048626654853562370, 2044320973630590977, '你好', '2026-04-27 13:00:23', '2026-04-27 13:00:23', 0);
INSERT INTO `application_message` VALUES (2058226640334385154, 2058226586236252161, 2044318541303296001, '测试在线沟通', '2026-05-24 00:40:46', '2026-05-24 00:40:46', 0);
INSERT INTO `application_message` VALUES (2066744035126579202, 2066742674527916033, 2044320973630590977, '你好', '2026-06-16 12:45:51', '2026-06-16 12:45:51', 0);

-- ----------------------------
-- Table structure for company_profile
-- ----------------------------
DROP TABLE IF EXISTS `company_profile`;
CREATE TABLE `company_profile`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '企业用户ID',
  `company_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业名称',
  `license_no` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营业执照号',
  `contact_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '企业介绍',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company_profile_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业资料' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of company_profile
-- ----------------------------
INSERT INTO `company_profile` VALUES (2044346935655084034, 2044320973630590977, '美团', '11', '11111', '1111', '1', '2026-04-15 17:27:46', '2026-04-15 17:27:46', 0);

-- ----------------------------
-- Table structure for credit_adjust_log
-- ----------------------------
DROP TABLE IF EXISTS `credit_adjust_log`;
CREATE TABLE `credit_adjust_log`  (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `delta` int NOT NULL COMMENT '分数变化',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `admin_username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_credit_adjust_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_credit_adjust_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '信用人工调整日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of credit_adjust_log
-- ----------------------------
INSERT INTO `credit_adjust_log` VALUES (2046058674570399745, 2044320973630590977, -10, '学生胜诉', 'xianyuewy', '2026-04-20 10:49:37', '2026-04-20 10:49:37', 0);

-- ----------------------------
-- Table structure for credit_dispute
-- ----------------------------
DROP TABLE IF EXISTS `credit_dispute`;
CREATE TABLE `credit_dispute`  (
  `id` bigint NOT NULL,
  `reporter_user_id` bigint NOT NULL,
  `target_user_id` bigint NOT NULL,
  `related_type` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `related_id` bigint NULL DEFAULT NULL,
  `reason` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `supplement_text` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '申诉人补充说明',
  `evidence_urls` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT 'JSON:证据文件路径数组',
  `status` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'PENDING',
  `admin_remark` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `resolved_at` datetime NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dispute_status`(`status` ASC) USING BTREE,
  INDEX `idx_dispute_target`(`target_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of credit_dispute
-- ----------------------------
INSERT INTO `credit_dispute` VALUES (2046056026416963586, 2044318541303296001, 2044320973630590977, 'APPLICATION', 2044337540137299970, '111', NULL, NULL, 'RESOLVED', '学生胜诉，企业下降10分，并处罚款', '2026-04-20 10:49:10', '2026-04-20 10:39:05', '2026-04-20 10:39:05', 0);
INSERT INTO `credit_dispute` VALUES (2048627571938131970, 2044318541303296001, 2044320973630590977, 'JOB', 2046791305075388418, '工作时间不符，薪资不符', NULL, NULL, 'REJECTED', '资料与实际不符', '2026-04-27 13:05:25', '2026-04-27 12:57:29', '2026-04-27 12:57:29', 0);

-- ----------------------------
-- Table structure for job
-- ----------------------------
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job`  (
  `id` bigint NOT NULL COMMENT '主键',
  `publisher_user_id` bigint NOT NULL COMMENT '发布者用户ID（企业账号）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '岗位描述',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作地点',
  `job_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位类型',
  `salary_text` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '薪资展示文案',
  `salary_min` int NULL DEFAULT NULL COMMENT '最低薪资(元)',
  `salary_max` int NULL DEFAULT NULL COMMENT '最高薪资(元)',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位联系电话',
  `contact_wechat` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信/沟通说明',
  `expire_at` datetime NULL DEFAULT NULL COMMENT '岗位过期时间',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/OFFLINE',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_job_status`(`status` ASC) USING BTREE,
  INDEX `idx_job_publisher`(`publisher_user_id` ASC) USING BTREE,
  INDEX `idx_job_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_job_job_type`(`job_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '兼职岗位' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of job
-- ----------------------------
INSERT INTO `job` VALUES (1700000000000000001, 2044320973630590977, '周末书店理货员', '负责图书整理、上架与读者引导；工作氛围轻松，适合细心耐心的同学。周末两天排班，午休 1 小时。', '郑州市中原区', '门店服务', '120元/天', 120, 150, '13800138001', '到店咨询店长', '2026-08-12 12:01:46', 'APPROVED', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (1700000000000000002, 2044320973630590977, '校园推广兼职', '在食堂、宿舍区进行新品试饮与问卷收集；提供话术培训，按完成量结算，多劳多得。', '河南工业大学（莲花街校区）', '推广', '25元/小时', 25, 35, '13800138002', '加微信备注「推广兼职」', '2026-06-28 12:01:46', 'APPROVED', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (1700000000000000003, 2044320973630590977, '高中数学家教（线上）', '为高二学生辅导函数与立体几何，每周 2 次、每次 1.5 小时；需试讲 20 分钟，通过后排课。', '线上 / 郑州市区可协商', '家教', '80元/小时', 80, 100, '13800138003', '简历发邮箱后短信联系', '2026-09-11 12:01:46', 'APPROVED', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (1700000000000000004, 2044320973630590977, '咖啡店周末店员', '点单、出杯、清洁与简单备料；有老员工带教，要求服务态度好，能适应周末高峰。', '郑州市金水区', '餐饮', '18元/小时', 18, 22, '13800138004', 'Boss 直搜店名预约面试', '2026-07-13 12:01:46', 'APPROVED', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (1700000000000000005, 2044320973630590977, '展会活动协助', '布展撤展、物料搬运、观众引导；需连做 3 天，包工作餐与水，结束当天结清工资。', '郑州国际会展中心', '活动执行', '200元/天', 200, 200, '13800138005', '发姓名+手机号报名', '2026-06-13 12:01:46', 'OFFLINE', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (1700000000000000006, 2044320973630590977, '短视频剪辑（按条）', '根据脚本与素材包剪辑 60s 内竖版视频；需熟练使用剪映或 PR，试剪 1 条通过后长期合作。', '远程', '新媒体', '80元/条起', 80, 150, NULL, '网盘链接与样片微信发送', '2026-11-10 12:01:46', 'APPROVED', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (1700000000000000007, 2044320973630590977, '仓库分拣小时工', '快递小件分拣、扫码贴单；白班/晚班可选，需站立作业，提供劳保手套。', '郑州市经开区', '仓储物流', '22元/小时', 22, 26, '13800138007', '携带身份证原件', '2026-05-28 12:01:46', 'OFFLINE', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (1700000000000000008, 2044320973630590977, '行政助理（暑期）', '整理资料、会议室预定、快递收发；要求熟练使用 Word/Excel，每周至少到岗 4 天。', '郑州市高新区', '行政', '3500元/月', 3500, 3500, '13800138008', '邮件标题写「暑期行政」', '2026-08-22 12:01:46', 'APPROVED', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (1700000000000000009, 2044320973630590977, '羽毛球馆前台', '会员办卡、场地预约、简单收银；晚班 18:00–22:00，可排班轮休。', '郑州市二七区', '门店服务', '20元/小时', 20, 24, '13800138009', NULL, '2026-07-28 12:01:46', 'APPROVED', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (1700000000000000010, 2044320973630590977, '问卷调查员（商场）', '在指定商场邀请顾客完成线上问卷，有保底+提成；需主动沟通，服从督导安排。', '郑州市多个商场', '调研', '120元/天+提成', 120, 200, '13800138010', '培训群二维码报名后获取', '2026-06-03 12:01:46', 'OFFLINE', NULL, '2026-05-14 12:01:46', '2026-05-14 12:01:46', 0);
INSERT INTO `job` VALUES (2044335761630810114, 2044320973630590977, '食堂兼职', '复责打饭等', '1', '1', '', NULL, NULL, NULL, NULL, NULL, 'APPROVED', NULL, '2026-04-15 16:43:22', '2026-04-15 16:43:22', 0);
INSERT INTO `job` VALUES (2044349295597006849, 2044320973630590977, 'a', 'b', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'APPROVED', NULL, '2026-04-15 17:37:09', '2026-04-15 17:37:09', 0);
INSERT INTO `job` VALUES (2046052267032891393, 2044320973630590977, '午餐兼职', '负责打杂外卖打包等', '一餐', '兼职', '', NULL, NULL, NULL, NULL, NULL, 'REJECTED', '111', '2026-04-20 10:24:09', '2026-04-20 10:24:09', 0);
INSERT INTO `job` VALUES (2046791305075388418, 2044320973630590977, '食堂兼职', '负责打杂', '一餐', '兼职', '100/天', 100, 100, '15600000000', '115600000000', '2026-05-26 12:42:50', 'OFFLINE', NULL, '2026-04-22 11:20:49', '2026-04-22 11:20:49', 0);
INSERT INTO `job` VALUES (2048327517695229953, 2044320973630590977, '示例标题1', '测试岗位描述', '高新区莲花街', '家教', '150/天', 150, 160, '15611111112', '15611111112', '2026-05-26 17:05:11', 'OFFLINE', NULL, '2026-04-26 17:05:11', '2026-04-26 17:05:11', 0);
INSERT INTO `job` VALUES (2048628015368339457, 2044320973630590977, '测试标题', '测试描述', '1111', '111', '100/天', 100, 150, '15640000000', '15640000000', '2026-05-27 12:59:15', 'PENDING', NULL, '2026-04-27 12:59:15', '2026-04-27 12:59:15', 0);
INSERT INTO `job` VALUES (2066482792889376769, 2044320973630590977, '杀人', '血腥暴力', '111', '11', '150', 150, 150, '15600001111', '15600001111', '2026-07-15 19:27:46', 'PENDING', 'AI存疑，需人工复核：AI审核异常，转人工处理', '2026-06-15 19:27:46', '2026-06-15 19:27:46', 0);
INSERT INTO `job` VALUES (2066483266371772417, 2044320973630590977, '防火打劫', '犯罪', '111', '111', '159', 100, 159, '15600001111', '15600001111', '2026-07-15 19:29:36', 'PENDING', 'AI存疑，需人工复核：AI审核异常，转人工处理', '2026-06-15 19:29:39', '2026-06-15 19:29:39', 0);
INSERT INTO `job` VALUES (2066484071678140418, 2044320973630590977, '防火打劫', '犯罪', '111', '111', '159', 100, 159, '15600001111', '15600001111', '2026-07-15 19:32:51', 'PENDING', 'AI存疑，需人工复核：AI审核异常，转人工处理', '2026-06-15 19:32:51', '2026-06-15 19:32:51', 0);
INSERT INTO `job` VALUES (2066484527787728898, 2044320973630590977, '打饭', '负责排号以及餐完成之后进行打饭', '金水区某某路', '食堂兼职', '100', 100, 100, '15649000999', '15649000999', '2026-07-15 19:34:40', 'PENDING', 'AI存疑，需人工复核：AI审核异常，转人工处理', '2026-06-15 19:34:40', '2026-06-15 19:34:40', 0);
INSERT INTO `job` VALUES (2066486191773044737, 2044320973630590977, '教小学生英语', '主要为两个月工作，每周上两节课，一节课1小时', '金水区莫某路', '家教', '100元/天', 80, 100, '15649000000', '15649000000', '2026-07-15 19:41:16', 'APPROVED', 'AI存疑，需人工复核：AI审核异常，转人工处理', '2026-06-15 19:41:16', '2026-06-15 19:41:16', 0);
INSERT INTO `job` VALUES (2066486729730183170, 2044320973630590977, '食堂兼职', '负责食堂打饭、排号、叫号以及打包', '一餐', '食堂兼职', '70r/天', 50, 50, '15649000999', '15649000999', '2026-07-15 19:43:24', 'OFFLINE', 'AI自动审核通过', '2026-06-15 19:43:24', '2026-06-15 20:06:00', 0);
INSERT INTO `job` VALUES (2066488348014374913, 2044320973630590977, '食堂兼职', '负责打饭以及排号', '金水区某某路', '食堂兼职', '150r/天', 150, 150, '15699999999', '15699999999', '2026-07-15 19:49:49', 'APPROVED', 'AI自动审核通过', '2026-06-15 19:49:50', '2026-06-15 19:49:50', 0);
INSERT INTO `job` VALUES (2066492604545007617, 2044320973630590977, '打劫', '打劫', '111', '111', '111', 111, 11, '1111111111', '1111111111', '2026-07-15 20:06:45', 'REJECTED', '驳回原因：岗位标题包含违法违规关键词，所有岗位信息均为无效乱码，属于违规无效内容 修改建议：请发布真实合法有效的兼职岗位信息，禁止涉及违法相关内容', '2026-06-15 20:06:45', '2026-06-15 20:06:45', 0);
INSERT INTO `job` VALUES (2066493313852190722, 2044320973630590977, '食堂兼职', '帮助进行打饭以及排号等流程', '第一餐厅', '食堂简直', '111', 111, 11, '1111111111', '1111111111', '2026-07-15 20:09:34', 'APPROVED', 'AI自动审核通过', '2026-06-15 20:09:34', '2026-06-16 12:45:08', 0);

-- ----------------------------
-- Table structure for job_application
-- ----------------------------
DROP TABLE IF EXISTS `job_application`;
CREATE TABLE `job_application`  (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `student_user_id` bigint NOT NULL COMMENT '学生用户ID',
  `company_user_id` bigint NOT NULL COMMENT '企业用户ID',
  `status` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/VIEWED/INTERVIEW/HIRED/REJECTED/CANCELLED',
  `intention` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '投递意向/备注',
  `interview_time` datetime NULL DEFAULT NULL COMMENT '面试时间',
  `interview_location` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '面试地点',
  `interview_note` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '面试说明',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_job_student_unique`(`job_id` ASC, `student_user_id` ASC) USING BTREE,
  INDEX `idx_application_student`(`student_user_id` ASC) USING BTREE,
  INDEX `idx_application_company`(`company_user_id` ASC) USING BTREE,
  INDEX `idx_application_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of job_application
-- ----------------------------
INSERT INTO `job_application` VALUES (2044337540137299970, 2044335761630810114, 2044318541303296001, 2044320973630590977, 'HIRED', '111', NULL, NULL, NULL, '2026-04-15 16:50:26', '2026-04-15 16:50:26', 0);
INSERT INTO `job_application` VALUES (2046804401919651841, 2044349295597006849, 2044318541303296001, 2044320973630590977, 'HIRED', '111', '2026-04-25 00:17:00', '111', '111', '2026-04-22 12:12:52', '2026-04-22 12:12:52', 0);
INSERT INTO `job_application` VALUES (2048263711820378113, 2046791305075388418, 2044318541303296001, 2044320973630590977, 'INTERVIEW', '111', '2026-04-27 00:11:00', '4333', '面试', '2026-04-26 12:51:38', '2026-04-26 12:51:38', 0);
INSERT INTO `job_application` VALUES (2048626654853562370, 2046791305075388418, 2048626318243889154, 2044320973630590977, 'HIRED', '111', '2026-04-27 18:00:00', '腾讯会议链接', '测试补充', '2026-04-27 12:53:51', '2026-04-27 12:53:51', 0);
INSERT INTO `job_application` VALUES (2058226586236252161, 1700000000000000010, 2044318541303296001, 2044320973630590977, 'VIEWED', '测试投递意向', NULL, NULL, NULL, '2026-05-24 00:40:33', '2026-05-24 00:40:33', 0);
INSERT INTO `job_application` VALUES (2059073813330599937, 1700000000000000006, 2044318541303296001, 2044320973630590977, 'VIEWED', '111', NULL, NULL, NULL, '2026-05-26 08:47:07', '2026-05-26 08:47:08', 0);
INSERT INTO `job_application` VALUES (2066742674527916033, 2066488348014374913, 2044318541303296001, 2044320973630590977, 'VIEWED', '11', NULL, NULL, NULL, '2026-06-16 12:40:26', '2026-06-16 12:40:26', 0);

-- ----------------------------
-- Table structure for job_favorite
-- ----------------------------
DROP TABLE IF EXISTS `job_favorite`;
CREATE TABLE `job_favorite`  (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `student_user_id` bigint NOT NULL COMMENT '学生用户ID',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_favorite_student_job`(`job_id` ASC, `student_user_id` ASC) USING BTREE,
  INDEX `idx_favorite_student`(`student_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of job_favorite
-- ----------------------------
INSERT INTO `job_favorite` VALUES (2046051613849735169, 2044349295597006849, 2044318541303296001, '2026-04-20 10:21:33', '2026-04-20 10:21:33', 0);
INSERT INTO `job_favorite` VALUES (2048266984774201345, 2046791305075388418, 2044318541303296001, '2026-04-26 13:04:39', '2026-04-26 13:04:39', 1);
INSERT INTO `job_favorite` VALUES (2058222596362629122, 1700000000000000009, 2044318541303296001, '2026-05-24 00:24:42', '2026-05-24 00:24:42', 0);
INSERT INTO `job_favorite` VALUES (2058222602708611073, 1700000000000000006, 2044318541303296001, '2026-05-24 00:24:43', '2026-05-24 00:24:43', 0);

-- ----------------------------
-- Table structure for job_review
-- ----------------------------
DROP TABLE IF EXISTS `job_review`;
CREATE TABLE `job_review`  (
  `id` bigint NOT NULL COMMENT '主键',
  `application_id` bigint NOT NULL COMMENT '申请ID',
  `from_user_id` bigint NOT NULL COMMENT '评价发起方',
  `to_user_id` bigint NOT NULL COMMENT '被评价方',
  `score` int NOT NULL COMMENT '评分 1-5',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_to_user`(`to_user_id` ASC) USING BTREE,
  INDEX `idx_review_application`(`application_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '兼职评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of job_review
-- ----------------------------
INSERT INTO `job_review` VALUES (2044350643923107842, 2044337540137299970, 2044318541303296001, 2044320973630590977, 5, '', '2026-04-15 17:42:30', '2026-04-15 17:42:30', 0);
INSERT INTO `job_review` VALUES (2048324024506892289, 2046804401919651841, 2044320973630590977, 2044318541303296001, 5, '工作认真', '2026-04-26 16:51:18', '2026-04-26 16:51:18', 0);
INSERT INTO `job_review` VALUES (2048627262322999298, 2046804401919651841, 2044318541303296001, 2044320973630590977, 5, '测试内容', '2026-04-27 12:56:16', '2026-04-27 12:56:16', 0);
INSERT INTO `job_review` VALUES (2048628618794467329, 2048626654853562370, 2044320973630590977, 2048626318243889154, 5, '', '2026-04-27 13:01:39', '2026-04-27 13:01:39', 0);

-- ----------------------------
-- Table structure for student_resume
-- ----------------------------
DROP TABLE IF EXISTS `student_resume`;
CREATE TABLE `student_resume`  (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '学生用户ID',
  `self_intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '自我介绍',
  `education` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教育经历',
  `skills` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '技能特长',
  `work_experience` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '工作/实践经历',
  `attachment_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简历附件URL',
  `attachment_original_name` varchar(260) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件原始文件名',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_resume_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_resume
-- ----------------------------
INSERT INTO `student_resume` VALUES (2046767940268769281, 2044318541303296001, '本人比较乐观开朗能吃苦耐劳', '京城大学', '擅长编程', '京城科技开发', '/uploads/resumes/2044318541303296001_b25aa9312ed3491996af8e0c1df0884c.pdf', '2025年上半年英语六级笔试准考证(452427200309182337).pdf', '2026-04-22 09:47:59', '2026-04-22 09:47:59', 0);
INSERT INTO `student_resume` VALUES (2048626923901386754, 2048626318243889154, NULL, NULL, NULL, NULL, '/uploads/resumes/2048626318243889154_107e6b879a7b4a3282286da92a57664b.pdf', '2025年上半年英语六级笔试准考证(452427200309182337).pdf', '2026-04-27 12:54:55', '2026-04-27 12:54:55', 0);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_key` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `config_value` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL,
  `remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`config_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES ('announcement', '版本更新，', '平台公告（可 HTML/纯文本）', '2026-04-27 13:05:42');
INSERT INTO `sys_config` VALUES ('banner_images', '[\"/uploads/banners/banner_34065825057b4a65a3c367db86cd69b6.png\",\"/uploads/banners/banner_1c27de0d511242ed95ae4d217a1264f5.png\",\"/uploads/banners/banner_b4b364c9bb8440568d30b7d3f2300505.png\",\"/uploads/banners/banner_8e1b91a95d93462d8324fc0d2c8825ab.png\"]', '首页轮播图 URL 列表 JSON 数组', '2026-05-25 18:30:20');
INSERT INTO `sys_config` VALUES ('credit_rules', '信用分初始100；良好行为加分，违约扣分；管理员可人工复核。', '信用规则说明', '2026-04-15 15:10:09');

-- ----------------------------
-- Table structure for system_notification
-- ----------------------------
DROP TABLE IF EXISTS `system_notification`;
CREATE TABLE `system_notification`  (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `biz_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `read_flag` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notify_user_read`(`user_id` ASC, `read_flag` ASC) USING BTREE,
  INDEX `idx_notify_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_notification
-- ----------------------------
INSERT INTO `system_notification` VALUES (2046804401919651842, 2044320973630590977, '收到新的岗位投递', '有学生投递了你的岗位：a', 'APPLICATION_SUBMITTED', '2046804401919651841', 1, '2026-04-22 12:12:52', '2026-04-22 12:12:52', 0);
INSERT INTO `system_notification` VALUES (2046805140666609666, 2044320973630590977, '学生已取消投递', '有一位学生取消了岗位投递（申请ID：2046804401919651841）', 'APPLICATION_CANCELLED', '2046804401919651841', 1, '2026-04-22 12:15:48', '2026-04-22 12:15:48', 0);
INSERT INTO `system_notification` VALUES (2046805178750889986, 2044320973630590977, '收到新的岗位投递', '有学生重新投递了你的岗位：a', 'APPLICATION_SUBMITTED', '2046804401919651841', 1, '2026-04-22 12:15:57', '2026-04-22 12:15:57', 0);
INSERT INTO `system_notification` VALUES (2047543272387178498, 2044318541303296001, '收到新的面试安排', '面试时间：2026-04-25T00:17，地点：111', 'INTERVIEW_SCHEDULE', '2046804401919651841', 1, '2026-04-24 13:08:52', '2026-04-24 13:08:52', 0);
INSERT INTO `system_notification` VALUES (2047543474225475585, 2044318541303296001, '投递状态已更新', '你的投递状态变更为：已录用', 'APPLICATION_STATUS', '2046804401919651841', 1, '2026-04-24 13:09:40', '2026-04-24 13:09:40', 0);
INSERT INTO `system_notification` VALUES (2048263711820378114, 2044320973630590977, '收到新的岗位投递', '有学生投递了你的岗位：食堂兼职', 'APPLICATION_SUBMITTED', '2048263711820378113', 1, '2026-04-26 12:51:38', '2026-04-26 12:51:38', 0);
INSERT INTO `system_notification` VALUES (2048263809430220801, 2044318541303296001, '收到新的面试安排', '面试时间：2026-04-27T00:11，地点：4333', 'INTERVIEW_SCHEDULE', '2048263711820378113', 1, '2026-04-26 12:52:02', '2026-04-26 12:52:02', 0);
INSERT INTO `system_notification` VALUES (2048626654853562371, 2044320973630590977, '收到新的岗位投递', '有学生投递了你的岗位：食堂兼职', 'APPLICATION_SUBMITTED', '2048626654853562370', 1, '2026-04-27 12:53:51', '2026-04-27 12:53:51', 0);
INSERT INTO `system_notification` VALUES (2048628501211348994, 2048626318243889154, '收到新的面试安排', '面试时间：2026-04-27T18:00，地点：腾讯会议链接', 'INTERVIEW_SCHEDULE', '2048626654853562370', 0, '2026-04-27 13:01:11', '2026-04-27 13:01:11', 0);
INSERT INTO `system_notification` VALUES (2048628558094499841, 2048626318243889154, '投递状态已更新', '你的投递状态变更为：已录用', 'APPLICATION_STATUS', '2048626654853562370', 0, '2026-04-27 13:01:25', '2026-04-27 13:01:25', 0);
INSERT INTO `system_notification` VALUES (2058226586299166721, 2044320973630590977, '收到新的岗位投递', '有学生投递了你的岗位：问卷调查员（商场）', 'APPLICATION_SUBMITTED', '2058226586236252161', 1, '2026-05-24 00:40:33', '2026-05-24 00:40:33', 0);
INSERT INTO `system_notification` VALUES (2059073813531926529, 2044320973630590977, '收到新的岗位投递', '有学生投递了你的岗位：短视频剪辑（按条）', 'APPLICATION_SUBMITTED', '2059073813330599937', 1, '2026-05-26 08:47:08', '2026-05-26 08:47:08', 0);
INSERT INTO `system_notification` VALUES (2066742674527916034, 2044320973630590977, '收到新的岗位投递', '有学生投递了你的岗位：食堂兼职', 'APPLICATION_SUBMITTED', '2066742674527916033', 1, '2026-06-16 12:40:27', '2026-06-16 12:40:27', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名/企业名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/avatars/default.png' COMMENT '头像路径',
  `role` enum('STUDENT','COMPANY','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户角色',
  `auth_status` enum('UNAUTH','PENDING','APPROVED','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UNAUTH' COMMENT '认证状态',
  `id_card` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学信网验证报告图片路径',
  `auth_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审核备注（审核不通过时填写原因）',
  `auth_time` datetime NULL DEFAULT NULL COMMENT '认证审核时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
  `account_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '账号是否启用 1-启用 0-禁用',
  `token_version` int NOT NULL DEFAULT 0 COMMENT '令牌版本',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2048626318243889155 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (2035185077501153281, 'xianyue', '$2a$10$FyiUFQyL9feD7SSyGClQZeXMBkp/P65TuY77WKk9l2BMVEw3xlthu', 'xianyue@163.com', '13911111111', NULL, NULL, '/avatars/default.png', 'ADMIN', 'UNAUTH', NULL, NULL, NULL, '2026-03-21 10:41:49', '2026-03-21 10:42:10', 0, 1, 0);
INSERT INTO `user` VALUES (2044262833438375938, 'xianyuewy', '$2a$10$dznF9/2oCYmpiw6gJi./K.Kq4/X8L3RyZ7G6INGWSP5BwZS96BC6.', 'xianyuewy13@163.com', '15649000990', 'xianyue', NULL, '/uploads/avatars/2044262833438375938_cce1316fd9114b3498c3c0e502e16e59.png', 'ADMIN', 'UNAUTH', NULL, NULL, NULL, '2026-04-15 11:53:35', '2026-04-15 11:54:34', 0, 1, 0);
INSERT INTO `user` VALUES (2044318541303296001, 'student', '$2a$10$GgTl5zvVCabqSKi/AoHU3enLscRQ6kPExPIDWgC.0mPnHp5wP.nHW', 'student@qq.com', '15611111111', '小李', '李元芳', '/uploads/avatars/2044318541303296001_d8c2a43e362240c1a3ad839cbab4bd59.png', 'STUDENT', 'APPROVED', '/uploads/student-auth/2044318541303296001_689863cd751e496ca364e8b6f25c2563.png', NULL, '2026-04-27 13:03:26', '2026-04-15 15:34:57', '2026-04-15 15:34:57', 0, 1, 1);
INSERT INTO `user` VALUES (2044318960968577026, 'stu1776238596', '$2a$10$M.kZWkJIyQKN2qPdq4PQ9.JTHkHL/kAEbFh3GWkkga9Mhn3jQGGdq', 'stu1776238596@test.com', '13800000000', NULL, NULL, '/avatars/default.png', 'STUDENT', 'UNAUTH', NULL, NULL, NULL, '2026-04-15 15:36:37', '2026-04-15 15:36:37', 0, 1, 0);
INSERT INTO `user` VALUES (2044320824460169218, 'company', '$2a$10$ZKbrgXZkemp2f764047AN.mkRXiJ53ZkDBItw7wWUxPSdMjBzY686', 'company@qq.com', '15622222222', NULL, NULL, '/avatars/default.png', 'STUDENT', 'UNAUTH', NULL, NULL, NULL, '2026-04-15 15:44:01', '2026-04-15 15:44:01', 0, 1, 0);
INSERT INTO `user` VALUES (2044320973630590977, 'company1', '$2a$10$pPrDxW0pRNtgc9BdqCVg.uoz0jmKepm8ZK869JbMW.8B9lpoU58zW', 'company1@qq.com', '15622222223', '小团子', '美团', '/uploads/avatars/2044320973630590977_53331944eba048deaf135f97c90fcdb9.png', 'COMPANY', 'APPROVED', NULL, NULL, '2026-04-15 17:42:03', '2026-04-15 15:44:36', '2026-04-15 15:44:36', 0, 1, 0);
INSERT INTO `user` VALUES (2048622591260774401, 'testuser', '$2a$10$nV1mNGsmdzaV4R4EiEvjD.n6Yj4Y9cd.BqlcUs3cWaZ2ARWhiF9Q6', 'test@test.com', '15622222222', NULL, NULL, '/avatars/default.png', 'STUDENT', 'UNAUTH', NULL, NULL, NULL, '2026-04-27 12:37:42', '2026-04-27 12:37:42', 0, 1, 0);
INSERT INTO `user` VALUES (2048626318243889154, 'testuser1', '$2a$10$mTG8sOvYwVZAiEioAVlFu.18y9cDUip9NkJnywrhgSAJprfqxksda', 'test1@teststudent.com', '15633333333', '张三', NULL, '/uploads/avatars/2048626318243889154_a480c9e873ec41f18ffddd2d62236324.png', 'STUDENT', 'UNAUTH', NULL, NULL, NULL, '2026-04-27 12:52:31', '2026-04-27 12:52:31', 0, 1, 0);

-- ----------------------------
-- Table structure for user_credit
-- ----------------------------
DROP TABLE IF EXISTS `user_credit`;
CREATE TABLE `user_credit`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `score` int NOT NULL DEFAULT 100 COMMENT '信用分 0-100',
  `credit_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NORMAL' COMMENT '等级标识',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_credit_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信用档案' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_credit
-- ----------------------------
INSERT INTO `user_credit` VALUES (2044318541475262465, 2044318541303296001, 100, 'EXCELLENT', '2026-04-15 15:34:57', '2026-04-15 15:34:57', 0);
INSERT INTO `user_credit` VALUES (2044318960968577027, 2044318960968577026, 100, 'EXCELLENT', '2026-04-15 15:36:37', '2026-04-15 15:36:37', 0);
INSERT INTO `user_credit` VALUES (2044320824472752130, 2044320824460169218, 100, 'EXCELLENT', '2026-04-15 15:44:01', '2026-04-15 15:44:01', 0);
INSERT INTO `user_credit` VALUES (2044320973693505538, 2044320973630590977, 90, 'EXCELLENT', '2026-04-15 15:44:36', '2026-04-15 15:44:36', 0);
INSERT INTO `user_credit` VALUES (2048622591432740865, 2048622591260774401, 100, 'EXCELLENT', '2026-04-27 12:37:42', '2026-04-27 12:37:42', 0);
INSERT INTO `user_credit` VALUES (2048626318273249282, 2048626318243889154, 100, 'EXCELLENT', '2026-04-27 12:52:31', '2026-04-27 12:52:31', 0);

SET FOREIGN_KEY_CHECKS = 1;
