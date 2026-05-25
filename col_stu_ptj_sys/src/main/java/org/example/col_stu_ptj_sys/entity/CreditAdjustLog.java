package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员人工调整信用分记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_adjust_log")
public class CreditAdjustLog extends com.colstuptjsys.entity.BaseEntity {

    private Long userId;

    /**
     * 分数变化（可正可负）
     */
    private Integer delta;

    private String reason;

    /**
     * 操作管理员用户名
     */
    private String adminUsername;
}
