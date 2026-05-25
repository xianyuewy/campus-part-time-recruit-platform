package org.example.col_stu_ptj_sys.service;

import org.example.col_stu_ptj_sys.dto.admin.DailyTrendPoint;
import org.example.col_stu_ptj_sys.dto.admin.DashboardStatsVO;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.enums.DisputeStatus;
import org.example.col_stu_ptj_sys.enums.JobStatus;
import org.example.col_stu_ptj_sys.entity.CreditDispute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserService userService;
    private final JobService jobService;
    private final CreditDisputeService creditDisputeService;

    public DashboardStatsVO stats() {
        long totalUsers = userService.count();
        long pendingUsers = userService.lambdaQuery()
                .eq(User::getAuthStatus, AuthStatus.PENDING)
                .count();
        long totalJobs = jobService.count();
        long pendingJobs = jobService.lambdaQuery()
                .eq(Job::getStatus, JobStatus.PENDING)
                .count();
        long disabledUsers = userService.lambdaQuery()
                .eq(User::getAccountEnabled, false)
                .count();
        long pendingDisputes = creditDisputeService.lambdaQuery()
                .eq(CreditDispute::getStatus, DisputeStatus.PENDING)
                .count();

        List<DailyTrendPoint> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.atTime(LocalTime.MAX);
            long nu = userService.lambdaQuery()
                    .ge(User::getCreateTime, start)
                    .le(User::getCreateTime, end)
                    .count();
            long nj = jobService.lambdaQuery()
                    .ge(Job::getCreateTime, start)
                    .le(Job::getCreateTime, end)
                    .count();
            trend.add(DailyTrendPoint.builder()
                    .date(day.toString())
                    .newUsers(nu)
                    .newJobs(nj)
                    .build());
        }

        return DashboardStatsVO.builder()
                .totalUsers(totalUsers)
                .pendingUsers(pendingUsers)
                .totalJobs(totalJobs)
                .pendingJobs(pendingJobs)
                .disabledUsers(disabledUsers)
                .pendingDisputes(pendingDisputes)
                .last7Days(trend)
                .build();
    }
}
