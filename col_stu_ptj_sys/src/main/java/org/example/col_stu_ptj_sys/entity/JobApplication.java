package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.col_stu_ptj_sys.enums.ApplicationStatus;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_application")
public class JobApplication extends com.colstuptjsys.entity.BaseEntity {
    private Long jobId;
    private Long studentUserId;
    private Long companyUserId;
    private ApplicationStatus status;
    private String intention;
    private LocalDateTime interviewTime;
    private String interviewLocation;
    private String interviewNote;
}
