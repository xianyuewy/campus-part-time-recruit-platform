package org.example.col_stu_ptj_sys.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationVO {
    private Long id;
    private String title;
    private String content;
    private String bizType;
    private String bizId;
    private Boolean read;
    private LocalDateTime createTime;
}
