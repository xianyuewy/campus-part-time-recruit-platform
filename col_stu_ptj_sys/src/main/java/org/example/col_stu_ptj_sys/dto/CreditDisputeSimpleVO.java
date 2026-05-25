package org.example.col_stu_ptj_sys.dto;

import lombok.Builder;
import lombok.Data;
import org.example.col_stu_ptj_sys.enums.DisputeStatus;

import java.util.List;

import java.time.LocalDateTime;

@Data
@Builder
public class CreditDisputeSimpleVO {
    private Long id;
    private Long reporterUserId;
    private String reporterUsername;
    private String reporterDisplayName;
    private Long targetUserId;
    private String targetUsername;
    private String targetDisplayName;
    /** 关联业务类型，便于核对 */
    private String relatedType;
    private Long relatedId;
    private String reason;
    private String supplementText;
    private List<String> evidenceUrls;
    private DisputeStatus status;
    private String adminRemark;
    private LocalDateTime createTime;
}
