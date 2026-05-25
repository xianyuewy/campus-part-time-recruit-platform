package org.example.col_stu_ptj_sys.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.PublicConfigVO;
import org.example.col_stu_ptj_sys.service.PublicConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公开配置", description = "无需登录：公告、轮播、规则")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicConfigController {

    private final PublicConfigService publicConfigService;

    @Operation(summary = "平台公开配置")
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<PublicConfigVO>> config() {
        return ResponseEntity.ok(ApiResponse.success(publicConfigService.getPublicConfig()));
    }
}
