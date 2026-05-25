package org.example.col_stu_ptj_sys.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * 信用争议中可选的关联业务类型（入库为字符串 code）
 */
@Getter
public enum CreditRelatedType {
    NONE("NONE", "无关联"),
    APPLICATION("APPLICATION", "岗位申请记录"),
    JOB("JOB", "岗位信息"),
    REVIEW("REVIEW", "双方互评记录");

    private final String code;
    private final String title;

    CreditRelatedType(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public static CreditRelatedType fromCode(String raw) {
        if (!StringUtils.hasText(raw)) {
            return NONE;
        }
        String u = raw.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(v -> v.code.equals(u))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的关联类型: " + raw));
    }
}
