package org.example.col_stu_ptj_sys.dto.student;

import lombok.Builder;
import lombok.Data;
import org.example.col_stu_ptj_sys.enums.ApplicationStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentApplicationVO {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long companyUserId;
    private String companyUsername;
    /** 对外展示：企业资料中的企业名或昵称或登录名 */
    private String companyDisplayName;
    private ApplicationStatus status;
    /** 中文状态，便于列表展示 */
    private String statusLabel;
    private String intention;
    private LocalDateTime interviewTime;
    private String interviewLocation;
    private String interviewNote;
    private LocalDateTime createTime;
}