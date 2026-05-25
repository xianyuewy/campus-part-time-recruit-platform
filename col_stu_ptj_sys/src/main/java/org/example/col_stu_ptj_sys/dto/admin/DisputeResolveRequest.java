package org.example.col_stu_ptj_sys.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DisputeResolveRequest {

    @NotBlank
    private String adminRemark;

    /**
     * RESOLVED：标记为已处理；REJECTED：驳回工单
     */
    @NotNull
    @Pattern(regexp = "^(RESOLVED|REJECTED)$")
    private String action;
}
