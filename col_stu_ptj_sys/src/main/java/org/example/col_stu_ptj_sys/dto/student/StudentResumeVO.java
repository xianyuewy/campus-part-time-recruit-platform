package org.example.col_stu_ptj_sys.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResumeVO {
    private Long userId;
    private String selfIntro;
    private String education;
    private String skills;
    private String workExperience;
    private String attachmentPath;
    private String attachmentOriginalName;
    private LocalDateTime updateTime;
}
