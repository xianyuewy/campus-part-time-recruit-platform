package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户信用分档案（一人一条）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_credit")
public class UserCredit extends com.colstuptjsys.entity.BaseEntity {

    private Long userId;

    /**
     * 信用分（百分制 0～100，与 {@link org.example.col_stu_ptj_sys.support.CreditLevelCalculator} 档一致）
     */
    private Integer score;

    /**
     * 信用等级展示文案
     */
    @TableField("credit_level")
    private String creditLevel;
}
