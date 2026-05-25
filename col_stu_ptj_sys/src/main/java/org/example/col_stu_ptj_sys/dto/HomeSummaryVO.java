package org.example.col_stu_ptj_sys.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前台首页欢迎区指标（按角色返回不同含义的 A/B 两项）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeSummaryVO {

    private long metricA;
    private String labelA;
    private long metricB;
    private String labelB;
}
