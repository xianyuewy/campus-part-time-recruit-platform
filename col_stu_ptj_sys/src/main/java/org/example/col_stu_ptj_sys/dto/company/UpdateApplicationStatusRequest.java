package org.example.col_stu_ptj_sys.dto.company;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateApplicationStatusRequest {
    @NotBlank
    private String status;
}
