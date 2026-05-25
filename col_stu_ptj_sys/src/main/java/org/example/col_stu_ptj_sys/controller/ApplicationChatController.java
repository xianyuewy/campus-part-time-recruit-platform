package org.example.col_stu_ptj_sys.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.chat.ApplicationMessageVO;
import org.example.col_stu_ptj_sys.dto.chat.SendApplicationMessageRequest;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.service.ApplicationChatService;
import org.example.col_stu_ptj_sys.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "投递沟通", description = "学生与企业就某条投递记录在线留言；支持 WebSocket 实时推送 /ws/application-chat")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ApplicationChatController {

    private final ApplicationChatService applicationChatService;
    private final UserService userService;

    private long currentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        return u.getId();
    }

    @Operation(summary = "某投递下的消息列表")
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationMessageVO>>> list(
            @PathVariable Long applicationId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationChatService.pageMessages(applicationId, currentUserId(), current, size)));
    }

    @Operation(summary = "发送一条消息")
    @PostMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<Void>> send(
            @PathVariable Long applicationId,
            @Valid @RequestBody SendApplicationMessageRequest req) {
        applicationChatService.sendMessage(applicationId, currentUserId(), req.getContent());
        return ResponseEntity.ok(ApiResponse.success("已发送", null));
    }
}
