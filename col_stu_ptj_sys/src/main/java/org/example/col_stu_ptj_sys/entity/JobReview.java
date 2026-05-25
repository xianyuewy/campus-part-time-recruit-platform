package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_review")
public class JobReview extends com.colstuptjsys.entity.BaseEntity {
    private Long applicationId;
    private Long fromUserId;
    private Long toUserId;
    private Integer score;
    private String content;
}
