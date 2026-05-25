-- 修改密码后使旧 JWT 失效：与 User.tokenVersion / JWT claim tv 配合使用
ALTER TABLE `user` ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 COMMENT '令牌版本' AFTER `account_enabled`;
