package org.example.col_stu_ptj_sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 认证状态枚举
 */
@Getter
public enum AuthStatus {
    UNAUTH("UNAUTH", "未认证"),
    PENDING("PENDING", "审核中"),
    APPROVED("APPROVED", "已认证"),
    REJECTED("REJECTED", "认证失败");

    @EnumValue
    private final String code;
    private final String desc;

    AuthStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}