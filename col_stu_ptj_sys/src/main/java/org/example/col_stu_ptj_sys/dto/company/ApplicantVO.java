package org.example.col_stu_ptj_sys.dto.company;

import lombok.Builder;
import lombok.Data;
import org.example.col_stu_ptj_sys.enums.ApplicationStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicantVO {
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private Long studentUserId;
    /** 企业端可见的称呼：优先昵称，否则真实姓名，否则统称「学生」（不向企业返回登录账号） */
    private String studentDisplayName;
    private String studentNickname;
    private String studentRealName;
    private String studentEmail;
    private String studentPhone;
    private ApplicationStatus status;
    /** 中文状态 */
    private String statusLabel;
    /** 是否已填写在线简历 */
    private Boolean hasResume;
    private String intention;
    private LocalDateTime interviewTime;
    private String interviewLocation;
    private String interviewNote;
    private LocalDateTime createTime;
}