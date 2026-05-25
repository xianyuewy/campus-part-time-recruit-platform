package org.example.col_stu_ptj_sys.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.admin.DashboardStatsVO;
import org.example.col_stu_ptj_sys.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员-数据看板", description = "平台关键指标与近7日趋势")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "看板统计")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsVO>> stats() {
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.stats()));
    }
}
