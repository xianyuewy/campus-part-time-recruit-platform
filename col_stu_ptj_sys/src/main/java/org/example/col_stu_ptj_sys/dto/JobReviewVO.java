package org.example.col_stu_ptj_sys.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobReviewVO {
    private Long id;
    private Long applicationId;
    private Long fromUserId;
    private String fromUsername;
    private String fromDisplayName;
    private Long toUserId;
    private String toUsername;
    private String toDisplayName;
    private Integer score;
    private String content;
    private LocalDateTime createTime;
}