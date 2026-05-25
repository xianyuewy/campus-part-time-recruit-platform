package org.example.col_stu_ptj_sys.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 启动时确保存在 user.token_version 列，避免因未手动执行 SQL 导致 JWT 校验阶段查询失败（500 认证系统错误）。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class TokenVersionColumnInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (url != null && !url.toLowerCase().contains("mysql")) {
                log.debug("跳过 token_version 自动补列（非 MySQL 数据源）");
                return;
            }
        } catch (Exception e) {
            log.warn("无法检测数据源类型，仍尝试添加 token_version: {}", e.getMessage());
        }

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE `user` ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 COMMENT '令牌版本'");
            log.info("已自动添加列 user.token_version");
        } catch (DataAccessException e) {
            Throwable t = e.getMostSpecificCause();
            String msg = t != null ? t.getMessage() : e.getMessage();
            if (msg != null && (msg.contains("Duplicate column") || msg.contains("1060"))) {
                log.debug("列 token_version 已存在，跳过");
            } else {
                log.error("自动添加 token_version 失败，请手动执行 db/schema-token-version.sql: {}", msg);
                throw e;
            }
        }
    }
}
