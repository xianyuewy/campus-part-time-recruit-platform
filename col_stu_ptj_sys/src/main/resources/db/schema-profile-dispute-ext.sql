-- 个人中心与争议补充材料（在已有库执行一次）
ALTER TABLE `credit_dispute`
  ADD COLUMN `supplement_text` text NULL COMMENT '申诉人补充说明' AFTER `reason`,
  ADD COLUMN `evidence_urls` text NULL COMMENT 'JSON:证据文件路径数组' AFTER `supplement_text`;
