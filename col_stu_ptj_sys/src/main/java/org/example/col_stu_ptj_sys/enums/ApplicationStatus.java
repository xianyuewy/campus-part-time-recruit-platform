package org.example.col_stu_ptj_sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ApplicationStatus {
    SUBMITTED("SUBMITTED", "待企业处理"),
    VIEWED("VIEWED", "企业已阅"),
    INTERVIEW("INTERVIEW", "面试沟通中"),
    HIRED("HIRED", "已录用"),
    REJECTED("REJECTED", "未录用"),
    CANCELLED("CANCELLED", "已取消投递");

    @EnumValue
    private final String code;
    private final String desc;

    ApplicationStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
