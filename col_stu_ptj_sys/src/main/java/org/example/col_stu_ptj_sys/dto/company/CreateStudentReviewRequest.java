package org.example.col_stu_ptj_sys.dto.company;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStudentReviewRequest {
    @NotNull
    private Long applicationId;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;
    private String content;
}