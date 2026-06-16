package org.example.col_stu_ptj_sys.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.dto.JobAuditResult;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.enums.JobStatus;
import org.example.col_stu_ptj_sys.service.JobService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AiJobAuditService {

    private final JobAuditAgent auditAgent;
    private final ObjectMapper objectMapper;
    private final JobService jobService;

    public AiJobAuditService(OpenAiChatModel chatModel, ObjectMapper objectMapper, JobService jobService) {
        this.auditAgent = AiServices.builder(JobAuditAgent.class)
                .chatLanguageModel(chatModel)
                .build();
        this.objectMapper = objectMapper;
        this.jobService = jobService;
    }

    /**
     * 执行AI岗位审核
     * 任何异常都会返回人工复核，不影响主业务流程
     */
    public JobAuditResult auditJob(String title, String description, String location,
                                   String jobType, String salaryText, Integer salaryMin,
                                   Integer salaryMax, Long companyUserId) {
        try {
            String jsonResult = auditAgent.audit(title, description, location, jobType,
                    salaryText, salaryMin, salaryMax, companyUserId);
            log.info("[AI岗位审核] 原始返回：{}", jsonResult);
            return objectMapper.readValue(jsonResult, JobAuditResult.class);
        } catch (Exception e) {
            log.error("[AI岗位审核] 审核异常，转人工复核", e);
            JobAuditResult fallback = new JobAuditResult();
            fallback.setResult("MANUAL");
            fallback.setRiskLevel("MEDIUM");
            fallback.setRejectReason("AI审核异常，转人工处理");
            return fallback;
        }
    }
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncAuditJob(Long jobId, String title, String description, String location,
                              String jobType, String salaryText, Integer salaryMin,
                              Integer salaryMax, Long companyUserId) {
        JobAuditResult result = auditJob(title, description, location, jobType,
                salaryText, salaryMin, salaryMax, companyUserId);

        Job job = new Job();
        job.setId(jobId);
        switch (result.getResult()) {
            case "PASS":
                job.setStatus(JobStatus.APPROVED);
                job.setAuditRemark("AI自动审核通过");
                break;
            case "REJECT":
                job.setStatus(JobStatus.REJECTED);
                job.setAuditRemark("驳回原因：" + result.getRejectReason()
                        + " 修改建议：" + result.getSuggestion());
                break;
            default:
                job.setStatus(JobStatus.PENDING);
                job.setAuditRemark("AI存疑，需人工复核：" + result.getRejectReason());
                break;
        }
        jobService.updateById(job);
    }
}