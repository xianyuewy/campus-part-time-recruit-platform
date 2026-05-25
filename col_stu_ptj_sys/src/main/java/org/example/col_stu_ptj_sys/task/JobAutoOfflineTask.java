package org.example.col_stu_ptj_sys.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.enums.JobStatus;
import org.example.col_stu_ptj_sys.service.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动下架到期岗位。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobAutoOfflineTask {

    private final JobService jobService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void offlineExpiredJobs() {
        LocalDateTime now = LocalDateTime.now();
        boolean updated = jobService.lambdaUpdate()
                .eq(Job::getStatus, JobStatus.APPROVED)
                .le(Job::getExpireAt, now)
                .set(Job::getStatus, JobStatus.OFFLINE)
                .update();
        if (updated) {
            log.info("已自动下架到期岗位，时间: {}", now);
        }
    }
}
