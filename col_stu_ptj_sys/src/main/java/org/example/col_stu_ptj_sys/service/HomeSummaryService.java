package org.example.col_stu_ptj_sys.service;

import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.HomeSummaryVO;
import org.example.col_stu_ptj_sys.entity.CreditDispute;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.entity.JobApplication;
import org.example.col_stu_ptj_sys.entity.JobFavorite;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.ApplicationStatus;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.enums.DisputeStatus;
import org.example.col_stu_ptj_sys.enums.JobStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 首页欢迎卡片统计（学生/企业/管理员）
 */
@Service
@RequiredArgsConstructor
public class HomeSummaryService {

    private final UserService userService;
    private final JobService jobService;
    private final JobApplicationService jobApplicationService;
    private final JobFavoriteService jobFavoriteService;
    private final CreditDisputeService creditDisputeService;

    public HomeSummaryVO summary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = userService.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        UserRole role = me.getRole();

        if (role == UserRole.STUDENT) {
            long apps = jobApplicationService.lambdaQuery()
                    .eq(JobApplication::getStudentUserId, me.getId())
                    .ne(JobApplication::getStatus, ApplicationStatus.CANCELLED)
                    .count();
            long favs = jobFavoriteService.lambdaQuery()
                    .eq(JobFavorite::getStudentUserId, me.getId())
                    .count();
            return HomeSummaryVO.builder()
                    .metricA(apps)
                    .labelA("有效投递")
                    .metricB(favs)
                    .labelB("收藏岗位")
                    .build();
        }

        if (role == UserRole.COMPANY) {
            long onShelf = jobService.lambdaQuery()
                    .eq(Job::getPublisherUserId, me.getId())
                    .eq(Job::getStatus, JobStatus.APPROVED)
                    .count();
            long inbox = jobApplicationService.lambdaQuery()
                    .eq(JobApplication::getCompanyUserId, me.getId())
                    .eq(JobApplication::getStatus, ApplicationStatus.SUBMITTED)
                    .count();
            return HomeSummaryVO.builder()
                    .metricA(onShelf)
                    .labelA("在招岗位")
                    .metricB(inbox)
                    .labelB("待处理投递")
                    .build();
        }

        if (role == UserRole.ADMIN) {
            long pendingUsers = userService.lambdaQuery()
                    .eq(User::getAuthStatus, AuthStatus.PENDING)
                    .count();
            long pendingJobs = jobService.lambdaQuery()
                    .eq(Job::getStatus, JobStatus.PENDING)
                    .count();
            long pendingDisputes = creditDisputeService.lambdaQuery()
                    .eq(CreditDispute::getStatus, DisputeStatus.PENDING)
                    .count();
            return HomeSummaryVO.builder()
                    .metricA(pendingUsers)
                    .labelA("待审用户")
                    .metricB(pendingJobs + pendingDisputes)
                    .labelB("待办（岗位+争议）")
                    .build();
        }

        return HomeSummaryVO.builder()
                .metricA(0)
                .labelA("—")
                .metricB(0)
                .labelB("—")
                .build();
    }
}
