package org.example.col_stu_ptj_sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum DisputeStatus {
    PENDING("PENDING", "待处理"),
    RESOLVED("RESOLVED", "已处理"),
    REJECTED("REJECTED", "已驳回");

    @EnumValue
    private final String code;
    private final String desc;

    DisputeStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
