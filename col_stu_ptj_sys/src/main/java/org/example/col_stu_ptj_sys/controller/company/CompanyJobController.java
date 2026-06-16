package org.example.col_stu_ptj_sys.controller.company;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.JobReviewVO;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.company.ApplicantVO;
import org.example.col_stu_ptj_sys.dto.student.StudentResumeVO;
import org.example.col_stu_ptj_sys.dto.company.CreateStudentReviewRequest;
import org.example.col_stu_ptj_sys.dto.company.PublishJobRequest;
import org.example.col_stu_ptj_sys.dto.company.ScheduleInterviewRequest;
import org.example.col_stu_ptj_sys.dto.company.UpdateApplicationStatusRequest;
import org.example.col_stu_ptj_sys.dto.company.UpsertCompanyProfileRequest;
import org.example.col_stu_ptj_sys.entity.CompanyProfile;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.service.company.CompanyJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "企业端", description = "企业资料、岗位管理、候选人处理、评价")
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyJobController {
    private final CompanyJobService companyJobService;

    @Operation(summary = "企业资料")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CompanyProfile>> myProfile() {
        return ResponseEntity.ok(ApiResponse.success(companyJobService.myProfile()));
    }

    @Operation(summary = "保存企业资料")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> upsertProfile(@Valid @RequestBody UpsertCompanyProfileRequest req) {
        companyJobService.upsertProfile(req);
        return ResponseEntity.ok(ApiResponse.success("保存成功", null));
    }

    @Operation(summary = "提交企业资质审核")
    @PostMapping("/profile/submit-audit")
    public ResponseEntity<ApiResponse<Void>> submitAudit() {
        companyJobService.submitQualificationAudit();
        return ResponseEntity.ok(ApiResponse.success("提交成功，请等待管理员审核", null));
    }

    @Operation(summary = "发布岗位")
    @PostMapping("/jobs")
    public ResponseEntity<ApiResponse<Void>> publish(@Valid @RequestBody PublishJobRequest req) {
        companyJobService.publishJob(req);
        return ResponseEntity.ok(ApiResponse.success("发布成功，等待审核", null));
    }

    @Operation(summary = "我的岗位")
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<PageResponse<Job>>> myJobs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(companyJobService.myJobs(current, size, keyword, jobType, status)));
    }

    @Operation(summary = "编辑岗位")
    @PutMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long jobId, @Valid @RequestBody PublishJobRequest req) {
        companyJobService.updateJob(jobId, req);
        return ResponseEntity.ok(ApiResponse.success("已更新", null));
    }

    @Operation(summary = "下架岗位")
    @PostMapping("/jobs/{jobId}/offline")
    public ResponseEntity<ApiResponse<Void>> offline(@PathVariable Long jobId) {
        companyJobService.offlineJob(jobId);
        return ResponseEntity.ok(ApiResponse.success("已下架", null));
    }

    @Operation(summary = "岗位申请列表")
    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<ApiResponse<PageResponse<ApplicantVO>>> applicants(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ResponseEntity.ok(ApiResponse.success(companyJobService.applicants(jobId, current, size)));
    }

    @Operation(summary = "查看某投递学生的简历（只读）")
    @GetMapping("/applications/{appId}/student-resume")
    public ResponseEntity<ApiResponse<StudentResumeVO>> applicantResume(@PathVariable Long appId) {
        return ResponseEntity.ok(ApiResponse.success(companyJobService.applicantStudentResume(appId)));
    }

    @Operation(summary = "企业收件箱（全部投递）")
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<PageResponse<ApplicantVO>>> myApplicants(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String jobTitle) {
        return ResponseEntity.ok(ApiResponse.success(companyJobService.myApplicants(current, size, status, jobTitle)));
    }

    @Operation(summary = "更新申请状态")
    @PostMapping("/applications/{appId}/status")
    public ResponseEntity<ApiResponse<Void>> updateAppStatus(
            @PathVariable Long appId,
            @Valid @RequestBody UpdateApplicationStatusRequest req) {
        companyJobService.updateApplicationStatus(appId, req.getStatus());
        return ResponseEntity.ok(ApiResponse.success("已更新", null));
    }

    @Operation(summary = "安排面试（时间/地点/说明）")
    @PostMapping("/applications/{appId}/interview-schedule")
    public ResponseEntity<ApiResponse<Void>> scheduleInterview(
            @PathVariable Long appId,
            @Valid @RequestBody ScheduleInterviewRequest req) {
        companyJobService.scheduleInterview(appId, req);
        return ResponseEntity.ok(ApiResponse.success("面试安排已发送", null));
    }

    @Operation(summary = "评价学生")
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<Void>> reviewStudent(@Valid @RequestBody CreateStudentReviewRequest req) {
        companyJobService.reviewStudent(req.getApplicationId(), req.getScore(), req.getContent());
        return ResponseEntity.ok(ApiResponse.success("评价成功", null));
    }

    @Operation(summary = "我发出的评价")
    @GetMapping("/reviews/given")
    public ResponseEntity<ApiResponse<PageResponse<JobReviewVO>>> myGivenReviews(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ResponseEntity.ok(ApiResponse.success(companyJobService.myGivenReviews(current, size)));
    }

    @Operation(summary = "我收到的评价")
    @GetMapping("/reviews/received")
    public ResponseEntity<ApiResponse<PageResponse<JobReviewVO>>> myReceivedReviews(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ResponseEntity.ok(ApiResponse.success(companyJobService.myReceivedReviews(current, size)));
    }
}