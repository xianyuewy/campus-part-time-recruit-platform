package org.example.col_stu_ptj_sys.dto.admin;

import lombok.Builder;
import lombok.Data;
import org.example.col_stu_ptj_sys.enums.DisputeStatus;

import java.util.List;

import java.time.LocalDateTime;

@Data
@Builder
public class CreditDisputeVO {
    private Long id;
    private Long reporterUserId;
    private String reporterUsername;
    private String reporterDisplayName;
    private Long targetUserId;
    private String targetUsername;
    private String targetDisplayName;
    private String relatedType;
    private Long relatedId;
    private String reason;
    private String supplementText;
    private List<String> evidenceUrls;
    private DisputeStatus status;
    private String adminRemark;
    private LocalDateTime resolvedAt;
    private LocalDateTime createTime;
}
