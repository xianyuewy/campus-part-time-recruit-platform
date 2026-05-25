package org.example.col_stu_ptj_sys.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreditAdjustRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Integer delta;

    @NotNull
    @Size(min = 1, max = 500)
    private String reason;
}
