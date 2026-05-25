package org.example.col_stu_ptj_sys.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCreditDisputeRequest {
    @NotNull
    private Long targetUserId;
    @NotBlank
    private String reason;
    /**
     * 可选：NONE / APPLICATION / JOB / REVIEW（与平台内业务记录对应，便于管理员核查）
     */
    private String relatedType;
    /**
     * 与 {@link #relatedType} 对应的业务主键：申请记录 id、岗位 id、评价记录 id；无关联时可不传
     */
    private Long relatedId;
}
