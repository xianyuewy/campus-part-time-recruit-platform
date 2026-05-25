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
 * 为 job 表补充岗位联系电话、微信说明（兼容已有库）。
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class SchemaJobContactInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (url != null && !url.toLowerCase(Locale.ROOT).contains("mysql")) {
                log.debug("跳过 job 联系字段迁移（非 MySQL）");
                return;
            }
        } catch (Exception e) {
            log.warn("检测数据源失败，仍尝试迁移 job 联系字段: {}", e.getMessage());
        }

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        for (String ddl : new String[]{
                "ALTER TABLE `job` ADD COLUMN `contact_phone` VARCHAR(32) NULL COMMENT '岗位联系电话' AFTER `salary_max`",
                "ALTER TABLE `job` ADD COLUMN `contact_wechat` VARCHAR(500) NULL COMMENT '微信/沟通说明' AFTER `contact_phone`"
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
        log.info("已确保 job 表含 contact_phone、contact_wechat");
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
