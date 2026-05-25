package org.example.col_stu_ptj_sys.dto.admin;

import lombok.Builder;
import lombok.Data;
import org.example.col_stu_ptj_sys.enums.JobStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class JobAuditVO {
    private Long id;
    private Long publisherUserId;
    /** 企业登录账号，便于与后台、日志对照 */
    private String publisherUsername;
    /** 发布者在个人中心填写的昵称 */
    private String publisherNickname;
    /** 企业资料中的企业名称 */
    private String publisherCompanyName;
    private String title;
    private String description;
    private String location;
    private String salaryText;
    private JobStatus status;
    private String auditRemark;
    private LocalDateTime createTime;
}
