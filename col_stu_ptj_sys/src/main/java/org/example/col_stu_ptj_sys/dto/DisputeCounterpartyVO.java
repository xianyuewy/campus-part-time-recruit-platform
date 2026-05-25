package org.example.col_stu_ptj_sys.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 发起争议时可选择的对方用户（来自历史投递/招聘往来）
 */
@Data
@Builder
public class DisputeCounterpartyVO {
    private Long userId;
    /** 登录账号 */
    private String username;
    /** 展示名：企业名称或学生用户名 */
    private String displayName;
    /** 辅助说明，如最近关联岗位 */
    private String hint;
}
