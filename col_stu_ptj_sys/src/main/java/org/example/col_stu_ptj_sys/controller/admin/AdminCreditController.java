package org.example.col_stu_ptj_sys.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.admin.CreditAdjustRequest;
import org.example.col_stu_ptj_sys.dto.admin.CreditDisputeVO;
import org.example.col_stu_ptj_sys.dto.admin.CreditProfileVO;
import org.example.col_stu_ptj_sys.dto.admin.DisputeResolveRequest;
import org.example.col_stu_ptj_sys.service.AdminCreditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-信用监管", description = "信用档案、人工调分、争议处理")
@RestController
@RequestMapping("/api/admin/credit")
@RequiredArgsConstructor
public class AdminCreditController {

    private final AdminCreditService adminCreditService;

    @Operation(summary = "信用档案分页")
    @GetMapping("/profiles")
    public ResponseEntity<ApiResponse<PageResponse<CreditProfileVO>>> profiles(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(
                adminCreditService.pageProfiles(current, size, keyword)));
    }

    @Operation(summary = "人工调整信用分")
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<Void>> adjust(@Valid @RequestBody CreditAdjustRequest request) {
        adminCreditService.adjustScore(request.getUserId(), request.getDelta(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }

    @Operation(summary = "争议工单分页")
    @GetMapping("/disputes")
    public ResponseEntity<ApiResponse<PageResponse<CreditDisputeVO>>> disputes(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(
                adminCreditService.pageDisputes(current, size, status)));
    }

    @Operation(summary = "处理争议工单")
    @PostMapping("/disputes/{id}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolve(
            @PathVariable Long id,
            @Valid @RequestBody DisputeResolveRequest request) {
        adminCreditService.resolveDispute(id, request.getAction(), request.getAdminRemark());
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }
}
