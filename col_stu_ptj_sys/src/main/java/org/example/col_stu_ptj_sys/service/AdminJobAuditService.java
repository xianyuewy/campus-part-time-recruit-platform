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

@Service
@RequiredArgsConstructor
public class AdminJobAuditService {

    private final JobService jobService;
    private final AdminJobVoAssembler adminJobVoAssembler;

    public PageResponse<JobAuditVO> pagePendingJobs(long current, long size) {
        Page<Job> page = new Page<>(current, size);
        Page<Job> result = jobService.lambdaQuery()
                .eq(Job::getStatus, JobStatus.PENDING)
                .orderByDesc(Job::getCreateTime)
                .page(page);
        Page<JobAuditVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(adminJobVoAssembler::toVo).toList());
        return PageResponse.of(voPage);
    }

    @Transactional
    public void approve(Long jobId) {
        Job job = jobService.getById(jobId);
        if (job == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (job.getStatus() != JobStatus.PENDING) {
            throw new BusinessException("当前状态不可审核通过");
        }
        job.setStatus(JobStatus.APPROVED);
        job.setAuditRemark(null);
        if (!jobService.updateById(job)) {
            throw new BusinessException("更新失败");
        }
    }

    @Transactional
    public void reject(Long jobId, String remark) {
        Job job = jobService.getById(jobId);
        if (job == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (job.getStatus() != JobStatus.PENDING) {
            throw new BusinessException("当前状态不可驳回");
        }
        job.setStatus(JobStatus.REJECTED);
        job.setAuditRemark(remark);
        if (!jobService.updateById(job)) {
            throw new BusinessException("更新失败");
        }
    }

}
