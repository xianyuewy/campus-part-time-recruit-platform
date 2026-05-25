package org.example.col_stu_ptj_sys.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.admin.AuditRemarkRequest;
import org.example.col_stu_ptj_sys.dto.admin.JobAuditVO;
import org.example.col_stu_ptj_sys.service.AdminJobAuditService;
import org.example.col_stu_ptj_sys.service.AdminJobManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-岗位审核", description = "待审核岗位列表与审核操作")
@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class AdminJobController {

    private final AdminJobAuditService adminJobAuditService;
    private final AdminJobManagementService adminJobManagementService;

    @Operation(summary = "岗位列表（全状态/筛选）")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobAuditVO>>> listJobs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        PageResponse<JobAuditVO> page = adminJobManagementService.pageJobs(current, size, status, keyword);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "下架岗位")
    @PostMapping("/{id}/offline")
    public ResponseEntity<ApiResponse<Void>> offline(@PathVariable Long id) {
        adminJobManagementService.offline(id);
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }

    @Operation(summary = "分页查询待审核岗位")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<JobAuditVO>>> pending(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        PageResponse<JobAuditVO> page = adminJobAuditService.pagePendingJobs(current, size);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long id) {
        adminJobAuditService.approve(id);
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }

    @Operation(summary = "审核驳回")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long id,
            @Valid @RequestBody AuditRemarkRequest request) {
        adminJobAuditService.reject(id, request.getRemark());
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }
}
