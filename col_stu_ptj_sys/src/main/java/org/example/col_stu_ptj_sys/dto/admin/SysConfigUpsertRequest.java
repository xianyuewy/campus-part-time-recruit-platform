package org.example.col_stu_ptj_sys.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysConfigUpsertRequest {

    @NotBlank
    private String configKey;

    private String configValue;

    private String remark;
}
