package org.example.col_stu_ptj_sys.dto.student;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpsertStudentResumeRequest {

    @Size(max = 5000)
    private String selfIntro;

    @Size(max = 5000)
    private String education;

    @Size(max = 5000)
    private String skills;

    @Size(max = 5000)
    private String workExperience;
}
