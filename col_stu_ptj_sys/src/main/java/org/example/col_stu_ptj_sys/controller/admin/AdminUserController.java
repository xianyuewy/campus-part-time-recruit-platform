package org.example.col_stu_ptj_sys.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.admin.AuditRemarkRequest;
import org.example.col_stu_ptj_sys.dto.admin.UserAuditVO;
import org.example.col_stu_ptj_sys.service.AdminUserAuditService;
import org.example.col_stu_ptj_sys.service.AdminUserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-用户审核", description = "待审核用户列表与审核操作")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserAuditService adminUserAuditService;
    private final AdminUserManagementService adminUserManagementService;

    @Operation(summary = "用户列表（筛选/搜索/启停）")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserAuditVO>>> listUsers(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean accountEnabled) {
        PageResponse<UserAuditVO> page = adminUserManagementService.pageUsers(
                current, size, role, keyword, accountEnabled);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "停用账号")
    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disable(@PathVariable Long id) {
        adminUserManagementService.disable(id);
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }

    @Operation(summary = "启用账号")
    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enable(@PathVariable Long id) {
        adminUserManagementService.enable(id);
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }

    @Operation(summary = "分页查询待审核用户")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<UserAuditVO>>> pending(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        PageResponse<UserAuditVO> page = adminUserAuditService.pagePendingUsers(current, size);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long id) {
        adminUserAuditService.approve(id);
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }

    @Operation(summary = "审核驳回")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long id,
            @Valid @RequestBody AuditRemarkRequest request) {
        adminUserAuditService.reject(id, request.getRemark());
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }
}
