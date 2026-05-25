package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.admin.JobAuditVO;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.enums.JobStatus;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.service.admin.AdminJobVoAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminJobManagementService {

    private final JobService jobService;
    private final AdminJobVoAssembler adminJobVoAssembler;

    public PageResponse<JobAuditVO> pageJobs(long current, long size, String status, String keyword) {
        Page<Job> page = new Page<>(current, size);
        var q = jobService.lambdaQuery();
        if (StringUtils.hasText(status)) {
            q.eq(Job::getStatus, JobStatus.valueOf(status.toUpperCase()));
        }
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(Job::getTitle, keyword).or().like(Job::getLocation, keyword));
        }
        q.orderByDesc(Job::getCreateTime);
        Page<Job> result = q.page(page);
        Page<JobAuditVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(adminJobVoAssembler::toVo).toList());
        return PageResponse.of(voPage);
    }

    @Transactional
    public void offline(Long jobId) {
        Job job = jobService.getById(jobId);
        if (job == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (job.getStatus() == JobStatus.OFFLINE) {
            throw new BusinessException("岗位已下架");
        }
        job.setStatus(JobStatus.OFFLINE);
        if (!jobService.updateById(job)) {
            throw new BusinessException("更新失败");
        }
    }

}
