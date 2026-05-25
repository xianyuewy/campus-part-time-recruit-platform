package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.col_stu_ptj_sys.enums.JobStatus;
import java.time.LocalDateTime;

/**
 * 兼职岗位（企业发布，管理员审核）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`job`")
public class Job extends com.colstuptjsys.entity.BaseEntity {

    /**
     * 发布者用户 ID（企业账号）
     */
    private Long publisherUserId;

    private String title;

    private String description;

    private String location;

    /** 岗位类型（如: 服务员/家教/推广） */
    private String jobType;

    /**
     * 薪资展示文案，如「150元/天」
     */
    @TableField("salary_text")
    private String salaryText;

    /** 薪资最小值（单位: 元） */
    private Integer salaryMin;

    /** 薪资最大值（单位: 元） */
    private Integer salaryMax;

    /** 岗位联系电话（便于学生投递后联系） */
    private String contactPhone;

    /** 微信账号、添加方式或沟通说明 */
    private String contactWechat;

    /** 岗位过期时间（超过该时间自动下架） */
    private LocalDateTime expireAt;

    @TableField("`status`")
    private JobStatus status;

    /**
     * 审核备注（驳回原因等）
     */
    private String auditRemark;
}
