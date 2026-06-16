package org.example.col_stu_ptj_sys.service.student;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.JobReviewVO;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.student.StudentApplicationVO;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.entity.JobApplication;
import org.example.col_stu_ptj_sys.entity.JobFavorite;
import org.example.col_stu_ptj_sys.entity.JobReview;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.ApplicationStatus;
import org.example.col_stu_ptj_sys.enums.JobStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.service.JobApplicationService;
import org.example.col_stu_ptj_sys.service.JobFavoriteService;
import org.example.col_stu_ptj_sys.service.JobReviewService;
import org.example.col_stu_ptj_sys.service.JobService;
import org.example.col_stu_ptj_sys.service.NotificationCenterService;
import org.example.col_stu_ptj_sys.service.UserDisplayService;
import org.example.col_stu_ptj_sys.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentJobService {
    private final JobService jobService;
    private final JobApplicationService jobApplicationService;
    private final JobFavoriteService jobFavoriteService;
    private final JobReviewService jobReviewService;
    private final UserService userService;
    private final NotificationCenterService notificationCenterService;
    private final UserDisplayService userDisplayService;

    public PageResponse<Job> pageJobs(long current, long size, String keyword, String jobType, String location) {
        var q = jobService.lambdaQuery()
                .eq(Job::getStatus, JobStatus.APPROVED)
                .and(w -> w.isNull(Job::getExpireAt).or().gt(Job::getExpireAt, LocalDateTime.now()));
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(Job::getTitle, keyword).or().like(Job::getDescription, keyword));
        }
        if (StringUtils.hasText(jobType)) {
            q.eq(Job::getJobType, jobType);
        }
        if (StringUtils.hasText(location)) {
            q.like(Job::getLocation, location);
        }
        var page = q.orderByDesc(Job::getCreateTime).page(new Page<>(current, size));
        return PageResponse.of(page);
    }

    public Job getJobDetail(Long jobId) {
        Job job = jobService.getById(jobId);
        if (job == null || job.getStatus() != JobStatus.APPROVED
                || (job.getExpireAt() != null && !job.getExpireAt().isAfter(LocalDateTime.now()))) {
            throw new BusinessException(404, "岗位不存在或已下架");
        }
        return job;
    }

    @Transactional
    public void apply(Long jobId, String intention) {
        User me = requireCurrentStudent();
        Job job = jobService.getById(jobId);
        if (job == null || job.getStatus() != JobStatus.APPROVED) {
            throw new BusinessException("岗位不可投递");
        }
        JobApplication active = jobApplicationService.lambdaQuery()
                .eq(JobApplication::getJobId, jobId)
                .eq(JobApplication::getStudentUserId, me.getId())
                .ne(JobApplication::getStatus, ApplicationStatus.CANCELLED)
                .one();
        if (active != null) {
            throw new BusinessException("你已投递该岗位");
        }
        JobApplication cancelled = jobApplicationService.lambdaQuery()
                .eq(JobApplication::getJobId, jobId)
                .eq(JobApplication::getStudentUserId, me.getId())
                .eq(JobApplication::getStatus, ApplicationStatus.CANCELLED)
                .one();
        if (cancelled != null) {
            cancelled.setStatus(ApplicationStatus.SUBMITTED);
            cancelled.setIntention(intention);
            cancelled.setCompanyUserId(job.getPublisherUserId());
            jobApplicationService.updateById(cancelled);
            notificationCenterService.notifyUser(
                    job.getPublisherUserId(),
                    "收到新的岗位投递",
                    "有学生重新投递了你的岗位：" + job.getTitle(),
                    "APPLICATION_SUBMITTED",
                    String.valueOf(cancelled.getId()));
            return;
        }
        JobApplication app = new JobApplication();
        app.setJobId(jobId);
        app.setStudentUserId(me.getId());
        app.setCompanyUserId(job.getPublisherUserId());
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setIntention(intention);
        jobApplicationService.save(app);
        notificationCenterService.notifyUser(
                job.getPublisherUserId(),
                "收到新的岗位投递",
                "有学生投递了你的岗位：" + job.getTitle(),
                "APPLICATION_SUBMITTED",
                String.valueOf(app.getId()));
    }

    public PageResponse<StudentApplicationVO> myApplications(long current, long size) {
        User me = requireCurrentStudent();
        return listApplicationsByStudentUserId(me.getId(), current, size);
    }

    /**
     * 按学生用户ID查询投递记录（供 AI 工具等无登录上下文场景使用）
     */
    public PageResponse<StudentApplicationVO> listApplicationsByStudentUserId(Long studentUserId, long current, long size) {
        if (studentUserId == null || studentUserId <= 0) {
            throw new BusinessException("学生用户ID无效");
        }
        User student = userService.getById(studentUserId);
        if (student == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessException("仅可查询学生用户的投递记录");
        }

        var page = jobApplicationService.lambdaQuery()
                .eq(JobApplication::getStudentUserId, studentUserId)
                .orderByDesc(JobApplication::getCreateTime)
                .page(new Page<>(current, size));

        Page<StudentApplicationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toApplicationVo).toList());
        return PageResponse.of(voPage);
    }

    private StudentApplicationVO toApplicationVo(JobApplication app) {
        Job job = jobService.getById(app.getJobId());
        User company = userService.getById(app.getCompanyUserId());
        return StudentApplicationVO.builder()
                .id(app.getId())
                .jobId(app.getJobId())
                .jobTitle(job != null ? job.getTitle() : null)
                .companyUserId(app.getCompanyUserId())
                .companyUsername(company != null ? company.getUsername() : null)
                .companyDisplayName(company != null ? userDisplayService.publicDisplayName(company) : null)
                .status(app.getStatus())
                .statusLabel(app.getStatus() != null ? app.getStatus().getDesc() : "")
                .intention(app.getIntention())
                .interviewTime(app.getInterviewTime())
                .interviewLocation(app.getInterviewLocation())
                .interviewNote(app.getInterviewNote())
                .createTime(app.getCreateTime())
                .build();
    }

    @Transactional
    public void cancelApplication(Long appId) {
        User me = requireCurrentStudent();
        JobApplication app = jobApplicationService.getById(appId);
        if (app == null || !me.getId().equals(app.getStudentUserId())) {
            throw new BusinessException(404, "投递不存在");
        }
        if (!(app.getStatus() == ApplicationStatus.SUBMITTED
                || app.getStatus() == ApplicationStatus.VIEWED
                || app.getStatus() == ApplicationStatus.INTERVIEW)) {
            throw new BusinessException("当前状态不可取消");
        }
        app.setStatus(ApplicationStatus.CANCELLED);
        jobApplicationService.updateById(app);
        notificationCenterService.notifyUser(
                app.getCompanyUserId(),
                "学生已取消投递",
                "有一位学生取消了岗位投递（申请ID：" + app.getId() + "）",
                "APPLICATION_CANCELLED",
                String.valueOf(app.getId()));
    }

    @Transactional
    public void reviewCompany(Long applicationId, Integer score, String content) {
        User me = requireCurrentStudent();
        JobApplication app = jobApplicationService.getById(applicationId);
        if (app == null || !me.getId().equals(app.getStudentUserId())) {
            throw new BusinessException(404, "投递不存在");
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
        r.setToUserId(app.getCompanyUserId());
        r.setScore(score);
        r.setContent(content);
        jobReviewService.save(r);
    }

    public PageResponse<JobReviewVO> myGivenReviews(long current, long size) {
        User me = requireCurrentStudent();
        return pageReviewsByFromUser(me.getId(), current, size);
    }

    public PageResponse<JobReviewVO> myReceivedReviews(long current, long size) {
        User me = requireCurrentStudent();
        return pageReviewsByToUser(me.getId(), current, size);
    }

    @Transactional
    public void favorite(Long jobId) {
        User me = requireCurrentStudent();
        long exists = jobFavoriteService.lambdaQuery()
                .eq(JobFavorite::getJobId, jobId)
                .eq(JobFavorite::getStudentUserId, me.getId())
                .count();
        if (exists > 0) return;
        JobFavorite f = new JobFavorite();
        f.setJobId(jobId);
        f.setStudentUserId(me.getId());
        jobFavoriteService.save(f);
    }

    @Transactional
    public void unfavorite(Long jobId) {
        User me = requireCurrentStudent();
        jobFavoriteService.lambdaUpdate()
                .eq(JobFavorite::getJobId, jobId)
                .eq(JobFavorite::getStudentUserId, me.getId())
                .remove();
    }

    public List<JobFavorite> myFavorites() {
        User me = requireCurrentStudent();
        return jobFavoriteService.lambdaQuery()
                .eq(JobFavorite::getStudentUserId, me.getId())
                .orderByDesc(JobFavorite::getCreateTime)
                .list();
    }

    private PageResponse<JobReviewVO> pageReviewsByFromUser(Long fromUserId, long current, long size) {
        var page = jobReviewService.lambdaQuery()
                .eq(JobReview::getFromUserId, fromUserId)
                .orderByDesc(JobReview::getCreateTime)
                .page(new Page<>(current, size));
        Page<JobReviewVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toReviewVO).toList());
        return PageResponse.of(voPage);
    }

    private PageResponse<JobReviewVO> pageReviewsByToUser(Long toUserId, long current, long size) {
        var page = jobReviewService.lambdaQuery()
                .eq(JobReview::getToUserId, toUserId)
                .orderByDesc(JobReview::getCreateTime)
                .page(new Page<>(current, size));
        Page<JobReviewVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toReviewVO).toList());
        return PageResponse.of(voPage);
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

    private User requireCurrentStudent() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(403, "仅学生可操作");
        }
        return user;
    }
}