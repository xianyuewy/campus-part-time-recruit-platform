-- =============================================================================
-- 演示岗位数据（MySQL 8+，库名默认 col_stu_ptj_sys）
-- 说明：
--   1. 学生端只展示 status = APPROVED 且未过期的岗位。
--   2. 每条 INSERT 从「第一个企业用户」取 publisher_user_id；若库中尚无企业账号，
--      请先在前台注册一个企业用户（或自行把子查询改成固定 user id）。
--   3. id 使用固定大整数，避免与线上雪花主键轻易冲突；若冲突请改 id 前缀。
--   4. 可重复执行：先按 id 删除演示行（最末段），再执行插入段。
-- =============================================================================

-- 如工具未默认选中库，请先：USE `col_stu_ptj_sys`;

-- 建议：重复导入前先删演示 id，避免主键冲突
DELETE FROM `job` WHERE `id` BETWEEN 1700000000000000001 AND 1700000000000000010;

-- ---------------------------------------------------------------------------
-- 插入 10 条「已上架」岗位（仅当存在企业用户时才会插入）
-- ---------------------------------------------------------------------------

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000001, x.id, '周末书店理货员',
'负责图书整理、上架与读者引导；工作氛围轻松，适合细心耐心的同学。周末两天排班，午休 1 小时。',
'郑州市中原区', '门店服务', '120元/天', 120, 150, '13800138001', '到店咨询店长', DATE_ADD(NOW(), INTERVAL 90 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000002, x.id, '校园推广兼职',
'在食堂、宿舍区进行新品试饮与问卷收集；提供话术培训，按完成量结算，多劳多得。',
'河南工业大学（莲花街校区）', '推广', '25元/小时', 25, 35, '13800138002', '加微信备注「推广兼职」', DATE_ADD(NOW(), INTERVAL 45 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000003, x.id, '高中数学家教（线上）',
'为高二学生辅导函数与立体几何，每周 2 次、每次 1.5 小时；需试讲 20 分钟，通过后排课。',
'线上 / 郑州市区可协商', '家教', '80元/小时', 80, 100, '13800138003', '简历发邮箱后短信联系', DATE_ADD(NOW(), INTERVAL 120 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000004, x.id, '咖啡店周末店员',
'点单、出杯、清洁与简单备料；有老员工带教，要求服务态度好，能适应周末高峰。',
'郑州市金水区', '餐饮', '18元/小时', 18, 22, '13800138004', 'Boss 直搜店名预约面试', DATE_ADD(NOW(), INTERVAL 60 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000005, x.id, '展会活动协助',
'布展撤展、物料搬运、观众引导；需连做 3 天，包工作餐与水，结束当天结清工资。',
'郑州国际会展中心', '活动执行', '200元/天', 200, 200, '13800138005', '发姓名+手机号报名', DATE_ADD(NOW(), INTERVAL 30 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000006, x.id, '短视频剪辑（按条）',
'根据脚本与素材包剪辑 60s 内竖版视频；需熟练使用剪映或 PR，试剪 1 条通过后长期合作。',
'远程', '新媒体', '80元/条起', 80, 150, NULL, '网盘链接与样片微信发送', DATE_ADD(NOW(), INTERVAL 180 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000007, x.id, '仓库分拣小时工',
'快递小件分拣、扫码贴单；白班/晚班可选，需站立作业，提供劳保手套。',
'郑州市经开区', '仓储物流', '22元/小时', 22, 26, '13800138007', '携带身份证原件', DATE_ADD(NOW(), INTERVAL 14 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000008, x.id, '行政助理（暑期）',
'整理资料、会议室预定、快递收发；要求熟练使用 Word/Excel，每周至少到岗 4 天。',
'郑州市高新区', '行政', '3500元/月', 3500, 3500, '13800138008', '邮件标题写「暑期行政」', DATE_ADD(NOW(), INTERVAL 100 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000009, x.id, '羽毛球馆前台',
'会员办卡、场地预约、简单收银；晚班 18:00–22:00，可排班轮休。',
'郑州市二七区', '门店服务', '20元/小时', 20, 24, '13800138009', NULL, DATE_ADD(NOW(), INTERVAL 75 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;

INSERT INTO `job` (`id`, `publisher_user_id`, `title`, `description`, `location`, `job_type`, `salary_text`, `salary_min`, `salary_max`, `contact_phone`, `contact_wechat`, `expire_at`, `status`, `audit_remark`, `create_time`, `update_time`, `deleted`)
SELECT 1700000000000000010, x.id, '问卷调查员（商场）',
'在指定商场邀请顾客完成线上问卷，有保底+提成；需主动沟通，服从督导安排。',
'郑州市多个商场', '调研', '120元/天+提成', 120, 200, '13800138010', '培训群二维码报名后获取', DATE_ADD(NOW(), INTERVAL 20 DAY), 'APPROVED', NULL, NOW(), NOW(), 0
FROM (SELECT `id` FROM `user` WHERE `role` = 'COMPANY' AND IFNULL(`deleted`, 0) = 0 ORDER BY `id` ASC LIMIT 1) x;
