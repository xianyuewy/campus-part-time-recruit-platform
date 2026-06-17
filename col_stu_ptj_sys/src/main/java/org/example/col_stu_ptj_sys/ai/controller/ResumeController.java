package org.example.col_stu_ptj_sys.ai.controller;

import org.example.col_stu_ptj_sys.ai.service.ResumeParseServiceImpl;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.student.ResumeParseResult;
import org.example.col_stu_ptj_sys.entity.StudentResume;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/resume")
public class ResumeController {

    private final ResumeParseServiceImpl resumeParseService;
    private final UserService userService;

    public ResumeController(ResumeParseServiceImpl resumeParseService, UserService userService) {
        this.resumeParseService = resumeParseService;
        this.userService = userService;
    }

    /**
     * 接口1：纯解析简历，返回结构化数据
     */
    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<ResumeParseResult>> parseResume(@RequestParam MultipartFile file) throws IOException {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(resumeParseService.parseResume(userId, file)));
    }

    /**
     * 接口2：解析并自动生成在线简历
     */
    @PostMapping("/auto-generate")
    public ResponseEntity<ApiResponse<StudentResume>> autoGenerateResume(@RequestParam MultipartFile file) throws IOException {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(resumeParseService.parseAndSaveResume(userId, file)));
    }

    /**
     * 提交解析任务（异步轮询用）
     * 完全对齐项目标准返回格式
     */
    @PostMapping("/parse/submit")
    public ResponseEntity<ApiResponse<String>> submitParseTask(@RequestParam MultipartFile file) throws IOException {
        Long userId = getCurrentUserId();
        String taskId = UUID.randomUUID().toString();

        // 主线程提前读取文件字节，避免异步线程文件流失效
        byte[] fileBytes = file.getBytes();
        String fileName = file.getOriginalFilename();
        resumeParseService.asyncParse(taskId, userId, fileBytes, fileName);

        return ResponseEntity.ok(ApiResponse.success(taskId));
    }

    /**
     * 轮询查询解析结果
     * 完全对齐项目标准返回格式
     */
    @GetMapping("/parse/result")
    public ResponseEntity<ApiResponse<ResumeParseResult>> getParseResult(@RequestParam String taskId) {
        ResumeParseResult result = resumeParseService.getResult(taskId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 从安全上下文获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        return user.getId();
    }
}