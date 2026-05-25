package org.example.col_stu_ptj_sys.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

/**
 * 迁移：通知中心、面试安排字段、岗位有效期字段。
 */
@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class SchemaNotifyInterviewInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (url != null && !url.toLowerCase(Locale.ROOT).contains("mysql")) {
                log.debug("跳过通知/面试/有效期迁移（非 MySQL）");
                return;
            }
        } catch (Exception e) {
            log.warn("检测数据源失败，仍尝试迁移通知/面试/有效期: {}", e.getMessage());
        }

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("""
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
                  KEY `idx_notify_user_read` (`user_id`,`read_flag`),
                  KEY `idx_notify_create_time` (`create_time`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        String[] ddls = new String[]{
                "ALTER TABLE `job` ADD COLUMN `expire_at` DATETIME NULL COMMENT '岗位过期时间' AFTER `contact_wechat`",
                "ALTER TABLE `job_application` ADD COLUMN `interview_time` DATETIME NULL COMMENT '面试时间' AFTER `intention`",
                "ALTER TABLE `job_application` ADD COLUMN `interview_location` VARCHAR(200) NULL COMMENT '面试地点' AFTER `interview_time`",
                "ALTER TABLE `job_application` ADD COLUMN `interview_note` VARCHAR(500) NULL COMMENT '面试说明' AFTER `interview_location`"
        };
        for (String ddl : ddls) {
            try {
                jdbc.execute(ddl);
            } catch (Exception e) {
                if (isDuplicateColumnError(e)) {
                    continue;
                }
                throw e;
            }
        }
        log.info("已确保通知中心、面试安排、岗位有效期字段可用");
    }

    private static boolean isDuplicateColumnError(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException se) {
                if (se.getErrorCode() == 1060 || "42S21".equals(se.getSQLState())) {
                    return true;
                }
            }
            String m = t.getMessage();
            if (m != null && m.toLowerCase(Locale.ROOT).contains("duplicate column")) {
                return true;
            }
        }
        return false;
    }
}
