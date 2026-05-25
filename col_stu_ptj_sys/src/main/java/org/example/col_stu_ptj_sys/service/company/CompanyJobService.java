package org.example.col_stu_ptj_sys.service.company;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.JobReviewVO;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.company.ApplicantVO;
import org.example.col_stu_ptj_sys.dto.company.PublishJobRequest;
import org.example.col_stu_ptj_sys.dto.company.ScheduleInterviewRequest;
import org.example.col_stu_ptj_sys.dto.company.UpsertCompanyProfileRequest;
import org.example.col_stu_ptj_sys.entity.CompanyProfile;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.entity.JobApplication;
import org.example.col_stu_ptj_sys.entity.JobReview;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.ApplicationStatus;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.enums.JobStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.service.CompanyProfileService;
import org.example.col_stu_ptj_sys.service.UserDisplayService;
import org.example.col_stu_ptj_sys.dto.student.StudentResumeVO;
import org.example.col_stu_ptj_sys.service.JobApplicationService;
import org.example.col_stu_ptj_sys.service.JobReviewService;
import org.example.col_stu_ptj_sys.service.JobService;
import org.example.col_stu_ptj_sys.service.NotificationCenterService;
import org.example.col_stu_ptj_sys.service.StudentResumeBusinessService;
import org.example.col_stu_ptj_sys.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompanyJobService {
    private final JobService jobService;
    private final JobApplicationService jobApplicationService;
    private final CompanyProfileService companyProfileService;
    private final JobReviewService jobReviewService;
    private final UserService userService;
    private final StudentResumeBusinessService studentResumeBusinessService;
    private final NotificationCenterService notificationCenterService;
    private final UserDisplayService userDisplayService;

    public CompanyProfile myProfile() {
        User me = requireCurrentCompany();
        return companyProfileService.lambdaQuery().eq(CompanyProfile::getUserId, me.getId()).one();
    }

    @Transactional
    public void upsertProfile(UpsertCompanyProfileRequest req) {
        User me = requireCurrentCompany();
        CompanyProfile exist = companyProfileService.lambdaQuery().eq(CompanyProfile::getUserId, me.getId()).one();
        if (exist == null) {
            CompanyProfile p = new CompanyProfile();
            p.setUserId(me.getId());
            p.setCompanyName(req.getCompanyName());
            p.setLicenseNo(req.getLicenseNo());
            p.setContactName(req.getContactName());
            p.setContactPhone(req.getContactPhone());
            p.setDescription(req.getDescription());
            companyProfileService.save(p);
        } else {
            exist.setCompanyName(req.getCompanyName());
            exist.setLicenseNo(req.getLicenseNo());
            exist.setContactName(req.getContactName());
            exist.setContactPhone(req.getContactPhone());
            exist.setDescription(req.getDescription());
            companyProfileService.updateById(exist);
        }
    }

    @Transactional
    public void publishJob(PublishJobRequest req) {
        User me = requireCurrentCompany();
        if (me.getAuthStatus() != AuthStatus.APPROVED) {
            throw new BusinessException(403, "企业资质未通过，暂不可发布岗位");
        }
        Job j = new Job();
        j.setPublisherUserId(me.getId());
        j.setTitle(req.getTitle());
        j.setDescription(req.getDescription());
        j.setLocation(req.getLocation());
        j.setJobType(req.getJobType());
        j.setSalaryText(req.getSalaryText());
        j.setSalaryMin(req.getSalaryMin());
        j.setSalaryMax(req.getSalaryMax());
        j.setContactPhone(trimToNull(req.getContactPhone()));
        j.setContactWechat(trimToNull(req.getContactWechat()));
        j.setExpireAt(normalizeExpireAt(req.getExpireAt()));
        j.setStatus(JobStatus.PENDING);
        jobService.save(j);
    }

    @Transactional
    public void submitQualificationAudit() {
        User me = requireCurrentCompany();
        if (me.getAuthStatus() == AuthStatus.PENDING) {
            throw new BusinessException("你已提交审核，请耐心等待");
        }
        if (me.getAuthStatus() == AuthStatus.APPROVED) {
            throw new BusinessException("企业已通过审核，无需重复提交");
        }
        CompanyProfile profile = companyProfileService.lambdaQuery()
                .eq(CompanyProfile::getUserId, me.getId())
                .one();
        if (profile == null) {
            throw new BusinessException("请先完善企业资料后再提交审核");
        }
        if (!StringUtils.hasText(profile.getCompanyName())
                || !StringUtils.hasText(profile.getLicenseNo())
                || !StringUtils.hasText(profile.getContactName())
                || !StringUtils.hasText(profile.getContactPhone())) {
            throw new BusinessException("请先补全企业名称、执照号、联系人和联系电话");
        }
        me.setAuthStatus(AuthStatus.PENDING);
        me.setAuthRemark(null);
        me.setAuthTime(null);
        userService.updateById(me);
    }

    public PageResponse<Job> myJobs(long current, long size) {
        User me = requireCurrentCompany();
        var page = jobService.lambdaQuery()
                .eq(Job::getPublisherUserId, me.getId())
                .orderByDesc(Job::getCreateTime)
                .page(new Page<>(current, size));
        return PageResponse.of(page);
    }

    @Transactional
    public void updateJob(Long jobId, PublishJobRequest req) {
        User me = requireCurrentCompany();
        Job j = requireCompanyJob(me.getId(), jobId);
        j.setTitle(req.getTitle());
        j.setDescription(req.getDescription());
        j.setLocation(req.getLocation());
        j.setJobType(req.getJobType());
        j.setSalaryText(req.getSalaryText());
        j.setSalaryMin(req.getSalaryMin());
        j.setSalaryMax(req.getSalaryMax());
        j.setContactPhone(trimToNull(req.getContactPhone()));
        j.setContactWechat(trimToNull(req.getContactWechat()));
        j.setExpireAt(normalizeExpireAt(req.getExpireAt()));
        j.setStatus(JobStatus.PENDING);
        jobService.updateById(j);
    }

    @Transactional
    public void offlineJob(Long jobId) {
        User me = requireCurrentCompany();
        Job j = requireCompanyJob(me.getId(), jobId);
        j.setStatus(JobStatus.OFFLINE);
        jobService.updateById(j);
    }

    @Transactional
    public PageResponse<ApplicantVO> applicants(Long jobId, long current, long size) {
        User me = requireCurrentCompany();
        requireCompanyJob(me.getId(), jobId);
        markSubmittedToViewedForJob(me.getId(), jobId);
        var page = jobApplicationService.lambdaQuery()
                .eq(JobApplication::getJobId, jobId)
                .orderByDesc(JobApplication::getCreateTime)
                .page(new Page<>(current, size));
        Page<ApplicantVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toApplicantVO).toList());
        return PageResponse.of(voPage);
    }

    @Transactional
    public PageResponse<ApplicantVO> myApplicants(long current, long size, String status) {
        User me = requireCurrentCompany();
        if (!StringUtils.hasText(status)) {
            markSubmittedToViewedForCompany(me.getId());
        }
        var query = jobApplicationService.lambdaQuery()
                .eq(JobApplication::getCompanyUserId, me.getId());
        if (StringUtils.hasText(status)) {
            try {
                query.eq(JobApplication::getStatus, ApplicationStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("状态参数不合法");
            }
        }
        var page = query.orderByDesc(JobApplication::getCreateTime)
                .page(new Page<>(current, size));
        Page<ApplicantVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toApplicantVO).toList());
        return PageResponse.of(voPage);
    }

    @Transactional
    public void updateApplicationStatus(Long appId, String status) {
        User me = requireCurrentCompany();
        JobApplication app = jobApplicationService.getById(appId);
        if (app == null || !me.getId().equals(app.getCompanyUserId())) {
            throw new BusinessException(404, "申请不存在");
        }
        ApplicationStatus newStatus = ApplicationStatus.valueOf(status.toUpperCase());
        app.setStatus(newStatus);
        jobApplicationService.updateById(app);
        notificationCenterService.notifyUser(
                app.getStudentUserId(),
                "投递状态已更新",
                "你的投递状态变更为：" + newStatus.getDesc(),
                "APPLICATION_STATUS",
                String.valueOf(app.getId()));
    }

    @Transactional
    public void scheduleInterview(Long appId, ScheduleInterviewRequest req) {
        User me = requireCurrentCompany();
        JobApplication app = jobApplicationService.getById(appId);
        if (app == null || !me.getId().equals(app.getCompanyUserId())) {
            throw new BusinessException(404, "申请不存在");
        }
        if (app.getStatus() == ApplicationStatus.CANCELLED || app.getStatus() == ApplicationStatus.REJECTED) {
            throw new BusinessException("当前申请状态不可安排面试");
        }
        app.setInterviewTime(req.getInterviewTime());
        app.setInterviewLocation(trimToNull(req.getInterviewLocation()));
        app.setInterviewNote(trimToNull(req.getInterviewNote()));
        app.setStatus(ApplicationStatus.INTERVIEW);
        jobApplicationService.updateById(app);
        notificationCenterService.notifyUser(
                app.getStudentUserId(),
                "收到新的面试安排",
                "面试时间：" + req.getInterviewTime() + "，地点：" + req.getInterviewLocation(),
                "INTERVIEW_SCHEDULE",
                String.valueOf(app.getId()));
    }

    public StudentResumeVO applicantStudentResume(Long applicationId) {
        User me = requireCurrentCompany();
        return studentResumeBusinessService.getForCompanyApplication(applicationId, me.getId());
    }

    @Transactional
    public void reviewStudent(Long applicationId, Integer score, String content) {
        User me = requireCurrentCompany();
        JobApplication app = jobApplicationService.getById(applicationId);
        if (app == null || !me.getId().equals(app.getCompanyUserId())) {
            throw new BusinessException(404, "申请不存在");
        }
        if (!(app.getStatus() == ApplicationStatus.HIRED || app.getStatus() == ApplicationStatus.CANCELLED)) {
            throw new BusinessException("仅已录用或已取消的记录可评价");
        }
        long exists = jobReviewService.lambdaQuery()
                .eq(JobReview::getApplicationId, applicationId)
                .eq(JobReview::getFromUserId, me.getId())
                .count();
        if (exists > 0) {
            throw new BusinessException("该记录已评价");
        }
        JobReview r = new JobReview();
        r.setApplicationId(applicationId);
        r.setFromUserId(me.getId());
        r.setToUserId(app.getStudentUserId());
        r.setScore(score);
        r.setContent(content);
        jobReviewService.save(r);
    }

    public PageResponse<JobReviewVO> myGivenReviews(long current, long size) {
        User me = requireCurrentCompany();
        var page = jobReviewService.lambdaQuery()
                .eq(JobReview::getFromUserId, me.getId())
                .orderByDesc(JobReview::getCreateTime)
                .page(new Page<>(current, size));
        Page<JobReviewVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toReviewVO).toList());
        return PageResponse.of(voPage);
    }

    public PageResponse<JobReviewVO> myReceivedReviews(long current, long size) {
        User me = requireCurrentCompany();
        var page = jobReviewService.lambdaQuery()
                .eq(JobReview::getToUserId, me.getId())
                .orderByDesc(JobReview::getCreateTime)
                .page(new Page<>(current, size));
        Page<JobReviewVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toReviewVO).toList());
        return PageResponse.of(voPage);
    }

    /**
     * 不向企业展示学生登录名；外显为：昵称、否则真实姓名、否则统称「学生」。
     */
    private static String studentDisplayNameForCompany(User stu) {
        if (stu == null) {
            return null;
        }
        if (StringUtils.hasText(stu.getNickname())) {
            return stu.getNickname().trim();
        }
        if (StringUtils.hasText(stu.getRealName())) {
            return stu.getRealName().trim();
        }
        return "学生";
    }

    private ApplicantVO toApplicantVO(JobApplication app) {
        User stu = userService.getById(app.getStudentUserId());
        Job job = jobService.getById(app.getJobId());
        return ApplicantVO.builder()
                .applicationId(app.getId())
                .jobId(app.getJobId())
                .jobTitle(job != null ? job.getTitle() : null)
                .studentUserId(app.getStudentUserId())
                .studentDisplayName(studentDisplayNameForCompany(stu))
                .studentNickname(stu != null ? stu.getNickname() : null)
                .studentRealName(stu != null ? stu.getRealName() : null)
                .studentEmail(stu != null ? stu.getEmail() : null)
                .studentPhone(stu != null ? stu.getPhone() : null)
                .status(app.getStatus())
                .statusLabel(app.getStatus() != null ? app.getStatus().getDesc() : "")
                .hasResume(studentResumeBusinessService.hasResume(app.getStudentUserId()))
                .intention(app.getIntention())
                .interviewTime(app.getInterviewTime())
                .interviewLocation(app.getInterviewLocation())
                .interviewNote(app.getInterviewNote())
                .createTime(app.getCreateTime())
                .build();
    }

    private JobReviewVO toReviewVO(JobReview r) {
        User from = userService.getById(r.getFromUserId());
        User to = userService.getById(r.getToUserId());
        return JobReviewVO.builder()
                .id(r.getId())
                .applicationId(r.getApplicationId())
                .fromUserId(r.getFromUserId())
                .fromUsername(from != null ? from.getUsername() : null)
                .fromDisplayName(from != null ? userDisplayService.publicDisplayName(from) : null)
                .toUserId(r.getToUserId())
                .toUsername(to != null ? to.getUsername() : null)
                .toDisplayName(to != null ? userDisplayService.publicDisplayName(to) : null)
                .score(r.getScore())
                .content(r.getContent())
                .createTime(r.getCreateTime())
                .build();
    }

    private void markSubmittedToViewedForJob(Long companyUserId, Long jobId) {
        jobApplicationService.lambdaUpdate()
                .eq(JobApplication::getCompanyUserId, companyUserId)
                .eq(JobApplication::getJobId, jobId)
                .eq(JobApplication::getStatus, ApplicationStatus.SUBMITTED)
                .set(JobApplication::getStatus, ApplicationStatus.VIEWED)
                .update();
    }

    private void markSubmittedToViewedForCompany(Long companyUserId) {
        jobApplicationService.lambdaUpdate()
                .eq(JobApplication::getCompanyUserId, companyUserId)
                .eq(JobApplication::getStatus, ApplicationStatus.SUBMITTED)
                .set(JobApplication::getStatus, ApplicationStatus.VIEWED)
                .update();
    }

    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private static LocalDateTime normalizeExpireAt(LocalDateTime expireAt) {
        LocalDateTime now = LocalDateTime.now();
        if (expireAt == null) {
            return now.plusDays(30);
        }
        if (!expireAt.isAfter(now.plusHours(1))) {
            throw new BusinessException("岗位过期时间至少晚于当前 1 小时");
        }
        return expireAt;
    }

    private Job requireCompanyJob(Long companyId, Long jobId) {
        Job j = jobService.getById(jobId);
        if (j == null || !companyId.equals(j.getPublisherUserId())) {
            throw new BusinessException(404, "岗位不存在");
        }
        return j;
    }

    private User requireCurrentCompany() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        if (user.getRole() != UserRole.COMPANY) {
            throw new BusinessException(403, "仅企业可操作");
        }
        return user;
    }
}