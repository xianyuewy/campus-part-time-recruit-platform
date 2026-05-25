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
 * 启动时创建学生简历与投递消息表（MySQL IF NOT EXISTS），避免未执行 SQL 导致功能不可用。
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class SchemaResumeChatInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (url != null && !url.toLowerCase().contains("mysql")) {
                log.debug("跳过简历/沟通表初始化（非 MySQL）");
                return;
            }
        } catch (Exception e) {
            log.warn("检测数据源失败，仍尝试创建表: {}", e.getMessage());
        }

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        try {
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS `student_resume` (
                      `id` BIGINT NOT NULL,
                      `user_id` BIGINT NOT NULL,
                      `self_intro` TEXT,
                      `education` TEXT,
                      `skills` TEXT,
                      `work_experience` TEXT,
                      `attachment_path` VARCHAR(512) DEFAULT NULL,
                      `attachment_original_name` VARCHAR(260) DEFAULT NULL,
                      `create_time` DATETIME DEFAULT NULL,
                      `update_time` DATETIME DEFAULT NULL,
                      `deleted` TINYINT NOT NULL DEFAULT 0,
                      PRIMARY KEY (`id`),
                      UNIQUE KEY `uk_resume_user` (`user_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS `application_message` (
                      `id` BIGINT NOT NULL,
                      `application_id` BIGINT NOT NULL,
                      `sender_user_id` BIGINT NOT NULL,
                      `content` VARCHAR(2000) NOT NULL,
                      `create_time` DATETIME DEFAULT NULL,
                      `update_time` DATETIME DEFAULT NULL,
                      `deleted` TINYINT NOT NULL DEFAULT 0,
                      PRIMARY KEY (`id`),
                      KEY `idx_app_msg_application` (`application_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            ensureResumeAttachmentColumns(jdbc);
            log.info("已确保存在表 student_resume、application_message");
        } catch (Exception e) {
            log.error("创建简历/沟通表失败，请手动执行 db/schema-resume-chat.sql: {}", e.getMessage());
            throw e;
        }
    }

    private void ensureResumeAttachmentColumns(JdbcTemplate jdbc) {
        for (String ddl : new String[]{
                "ALTER TABLE `student_resume` ADD COLUMN `attachment_path` VARCHAR(512) NULL COMMENT '简历附件URL' AFTER `work_experience`",
                "ALTER TABLE `student_resume` ADD COLUMN `attachment_original_name` VARCHAR(260) NULL COMMENT '附件原始文件名' AFTER `attachment_path`"
        }) {
            try {
                jdbc.execute(ddl);
            } catch (Exception e) {
                if (isDuplicateColumnError(e)) {
                    continue;
                }
                throw e;
            }
        }
    }

    /** Spring 往往包装为 BadSqlGrammarException，需在 cause 链上识别 MySQL 重复列 */
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
