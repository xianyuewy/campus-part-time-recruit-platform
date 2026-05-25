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
 * 确保 user 表存在 nickname、real_name 列，用于昵称与真实姓名/企业名称。
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class UserDisplayColumnInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (url != null && !url.toLowerCase().contains("mysql")) {
                log.debug("跳过 nickname/real_name 自动补列（非 MySQL 数据源）");
                return;
            }
        } catch (Exception e) {
            log.warn("无法检测数据源类型，仍尝试添加 nickname/real_name: {}", e.getMessage());
        }

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        addColumn(jdbc, "ALTER TABLE `user` ADD COLUMN `nickname` VARCHAR(64) NULL COMMENT '昵称' AFTER `phone`", "nickname");
        addColumn(
                jdbc,
                "ALTER TABLE `user` ADD COLUMN `real_name` VARCHAR(64) NULL COMMENT '真实姓名/企业名称' AFTER `nickname`",
                "real_name");
    }

    private static void addColumn(JdbcTemplate jdbc, String sql, String logicalName) {
        try {
            jdbc.execute(sql);
            log.info("已自动添加列 user.{}", logicalName);
        } catch (DataAccessException e) {
            Throwable t = e.getMostSpecificCause();
            String msg = t != null ? t.getMessage() : e.getMessage();
            if (msg != null && (msg.contains("Duplicate column") || msg.contains("1060"))) {
                log.debug("列 {} 已存在，跳过", logicalName);
            } else {
                log.error("自动添加列 {} 失败，请手动执行 db/schema-user-display.sql: {}", logicalName, msg);
                throw e;
            }
        }
    }
}
