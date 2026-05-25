package org.example.col_stu_ptj_sys.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.admin.SysConfigItemVO;
import org.example.col_stu_ptj_sys.dto.admin.SysConfigUpsertRequest;
import org.example.col_stu_ptj_sys.service.AdminBannerUploadService;
import org.example.col_stu_ptj_sys.service.AdminConfigService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "管理员-系统配置", description = "公告、轮播、信用规则等")
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final AdminConfigService adminConfigService;
    private final AdminBannerUploadService adminBannerUploadService;

    @Operation(summary = "全部配置")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SysConfigItemVO>>> list() {
        return ResponseEntity.ok(ApiResponse.success(adminConfigService.listAll()));
    }

    @Operation(summary = "单条配置")
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SysConfigItemVO>> get(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success(adminConfigService.getOne(key)));
    }

    @Operation(summary = "新增或更新配置")
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> upsert(@Valid @RequestBody SysConfigUpsertRequest request) {
        adminConfigService.upsert(request.getConfigKey(), request.getConfigValue(), request.getRemark());
        return ResponseEntity.ok(ApiResponse.success("保存成功", null));
    }

    @Operation(summary = "上传首页轮播图（返回 /uploads/banners/... 路径，写入 banner_images 的 JSON 数组）")
    @PostMapping(value = "/banner-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadBannerImage(@RequestPart("file") MultipartFile file) {
        String url = adminBannerUploadService.saveBanner(file);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url)));
    }
}
