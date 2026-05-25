package org.example.col_stu_ptj_sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 兼职岗位审核/上架状态
 */
@Getter
public enum JobStatus {
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已上架"),
    REJECTED("REJECTED", "已驳回"),
    OFFLINE("OFFLINE", "已下架");

    @EnumValue
    private final String code;
    private final String desc;

    JobStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
