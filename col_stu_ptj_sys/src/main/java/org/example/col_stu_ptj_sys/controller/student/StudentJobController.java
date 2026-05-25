package org.example.col_stu_ptj_sys.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.JobReviewVO;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.student.ApplyJobRequest;
import org.example.col_stu_ptj_sys.dto.student.CreateCompanyReviewRequest;
import org.example.col_stu_ptj_sys.dto.student.StudentApplicationVO;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.entity.JobFavorite;
import org.example.col_stu_ptj_sys.service.student.StudentJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学生端", description = "岗位浏览、投递、收藏、进度、评价")
@RestController
@RequestMapping("/api/student/jobs")
@RequiredArgsConstructor
public class StudentJobController {
    private final StudentJobService studentJobService;

    @Operation(summary = "岗位列表")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Job>>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(ApiResponse.success(
                studentJobService.pageJobs(current, size, keyword, jobType, location)));
    }

    @Operation(summary = "投递岗位")
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Void>> apply(@Valid @RequestBody ApplyJobRequest req) {
        studentJobService.apply(req.getJobId(), req.getIntention());
        return ResponseEntity.ok(ApiResponse.success("投递成功", null));
    }

    @Operation(summary = "我的投递")
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<PageResponse<StudentApplicationVO>>> myApplications(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ResponseEntity.ok(ApiResponse.success(studentJobService.myApplications(current, size)));
    }

    @Operation(summary = "取消投递")
    @PostMapping("/applications/{appId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelApplication(@PathVariable Long appId) {
        studentJobService.cancelApplication(appId);
        return ResponseEntity.ok(ApiResponse.success("已取消", null));
    }

    @Operation(summary = "评价企业")
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<Void>> reviewCompany(@Valid @RequestBody CreateCompanyReviewRequest req) {
        studentJobService.reviewCompany(req.getApplicationId(), req.getScore(), req.getContent());
        return ResponseEntity.ok(ApiResponse.success("评价成功", null));
    }

    @Operation(summary = "我发出的评价")
    @GetMapping("/reviews/given")
    public ResponseEntity<ApiResponse<PageResponse<JobReviewVO>>> myGivenReviews(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ResponseEntity.ok(ApiResponse.success(studentJobService.myGivenReviews(current, size)));
    }

    @Operation(summary = "我收到的评价")
    @GetMapping("/reviews/received")
    public ResponseEntity<ApiResponse<PageResponse<JobReviewVO>>> myReceivedReviews(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ResponseEntity.ok(ApiResponse.success(studentJobService.myReceivedReviews(current, size)));
    }

    @Operation(summary = "收藏岗位")
    @PostMapping("/{jobId}/favorite")
    public ResponseEntity<ApiResponse<Void>> favorite(@PathVariable Long jobId) {
        studentJobService.favorite(jobId);
        return ResponseEntity.ok(ApiResponse.success("收藏成功", null));
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{jobId}/favorite")
    public ResponseEntity<ApiResponse<Void>> unfavorite(@PathVariable Long jobId) {
        studentJobService.unfavorite(jobId);
        return ResponseEntity.ok(ApiResponse.success("已取消收藏", null));
    }

    @Operation(summary = "我的收藏")
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<JobFavorite>>> favorites() {
        return ResponseEntity.ok(ApiResponse.success(studentJobService.myFavorites()));
    }

    @Operation(summary = "岗位详情（已上架）")
    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<Job>> detail(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(studentJobService.getJobDetail(jobId)));
    }
}