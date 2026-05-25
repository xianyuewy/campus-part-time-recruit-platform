package org.example.col_stu_ptj_sys.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsVO {
    private long totalUsers;
    private long pendingUsers;
    private long totalJobs;
    private long pendingJobs;

    /** 已停用账号数 */
    private long disabledUsers;

    /** 待处理信用争议数 */
    private long pendingDisputes;

    /**
     * 近 7 天每日新增用户与岗位（按日对齐）
     */
    private List<DailyTrendPoint> last7Days;
}
