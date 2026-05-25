package org.example.col_stu_ptj_sys.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyJobRequest {
    @NotNull
    private Long jobId;
    @NotBlank
    private String intention;
}
