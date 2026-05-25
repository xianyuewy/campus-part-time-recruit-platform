package org.example.col_stu_ptj_sys.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditRemarkRequest {

    @NotBlank(message = "备注不能为空")
    private String remark;
}
