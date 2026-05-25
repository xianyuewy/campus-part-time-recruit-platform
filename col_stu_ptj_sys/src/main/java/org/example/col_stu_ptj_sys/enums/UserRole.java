package org.example.col_stu_ptj_sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {
    STUDENT("STUDENT", "学生"),
    COMPANY("COMPANY", "企业"),
    ADMIN("ADMIN", "管理员");

    @EnumValue // 标记这个字段的值存入数据库
    private final String code;
    private final String desc;

    UserRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}