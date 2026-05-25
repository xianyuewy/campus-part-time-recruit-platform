-- 昵称与真实姓名 / 企业名称（可单独执行，或由 UserDisplayColumnInitializer 自动补列）
ALTER TABLE `user` ADD COLUMN `nickname` VARCHAR(64) NULL COMMENT '昵称' AFTER `phone`;
ALTER TABLE `user` ADD COLUMN `real_name` VARCHAR(64) NULL COMMENT '真实姓名/企业名称' AFTER `nickname`;
