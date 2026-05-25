package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.col_stu_ptj_sys.enums.DisputeStatus;

import java.time.LocalDateTime;

/**
 * 信用评价争议工单（后续可与具体评价记录关联）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_dispute")
public class CreditDispute extends com.colstuptjsys.entity.BaseEntity {

    private Long reporterUserId;

    private Long targetUserId;

    /**
     * 关联类型占位：REVIEW / JOB 等
     */
    private String relatedType;

    private Long relatedId;

    private String reason;

    /**
     * 申诉人后续补充说明（可与发起时的 reason 区分，用于流程化补充）
     */
    private String supplementText;

    /**
     * JSON 数组：证据文件访问路径
     */
    private String evidenceUrls;

    private DisputeStatus status;

    private String adminRemark;

    private LocalDateTime resolvedAt;
}
