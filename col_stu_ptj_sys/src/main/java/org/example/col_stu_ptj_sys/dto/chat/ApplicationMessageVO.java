package org.example.col_stu_ptj_sys.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationMessageVO {
    private Long id;
    private Long applicationId;
    private Long senderUserId;
    private String senderUsername;
    private String senderDisplayName;
    private String content;
    private LocalDateTime createTime;
}
