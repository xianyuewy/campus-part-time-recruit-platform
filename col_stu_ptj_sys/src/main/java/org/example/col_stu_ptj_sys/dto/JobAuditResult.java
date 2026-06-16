package org.example.col_stu_ptj_sys.dto;

import lombok.Data;

/**
 * AI岗位审核结果
 */
@Data
public class JobAuditResult {
    /**
     * 审核结论：PASS-通过 REJECT-驳回 MANUAL-人工复核
     */
    private String result;

    /**
     * 风险等级：LOW/MEDIUM/HIGH
     */
    private String riskLevel;

    /**
     * 违规原因/驳回理由（多条用分号分隔）
     */
    private String rejectReason;

    /**
     * 修改建议
     */
    private String suggestion;
}