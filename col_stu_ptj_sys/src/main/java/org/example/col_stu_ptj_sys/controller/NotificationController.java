package org.example.col_stu_ptj_sys.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.notification.NotificationVO;
import org.example.col_stu_ptj_sys.service.NotificationCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "通知中心", description = "系统通知、未读计数、已读管理")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationCenterService notificationCenterService;

    @Operation(summary = "我的通知分页")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationVO>>> mine(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size) {
        return ResponseEntity.ok(ApiResponse.success(notificationCenterService.myNotifications(current, size)));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", notificationCenterService.unreadCount())));
    }

    @Operation(summary = "标记单条已读")
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        notificationCenterService.markRead(id);
        return ResponseEntity.ok(ApiResponse.success("已标记", null));
    }

    @Operation(summary = "全部已读")
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        notificationCenterService.markAllRead();
        return ResponseEntity.ok(ApiResponse.success("已全部标记", null));
    }
}
