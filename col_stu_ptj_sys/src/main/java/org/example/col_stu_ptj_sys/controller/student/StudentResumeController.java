package org.example.col_stu_ptj_sys.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.student.StudentResumeVO;
import org.example.col_stu_ptj_sys.dto.student.UpsertStudentResumeRequest;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.service.StudentResumeBusinessService;
import org.example.col_stu_ptj_sys.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "学生端-简历", description = "在线简历维护")
@RestController
@RequestMapping("/api/student/resume")
@RequiredArgsConstructor
public class StudentResumeController {

    private final StudentResumeBusinessService studentResumeBusinessService;
    private final UserService userService;

    private User requireStudent() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        if (u.getRole() != UserRole.STUDENT) {
            throw new BusinessException(403, "仅学生可维护简历");
        }
        return u;
    }

    @Operation(summary = "我的简历")
    @GetMapping
    public ResponseEntity<ApiResponse<StudentResumeVO>> mine() {
        User me = requireStudent();
        return ResponseEntity.ok(ApiResponse.success(studentResumeBusinessService.getMine(me.getId())));
    }

    @Operation(summary = "保存简历")
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> save(@Valid @RequestBody UpsertStudentResumeRequest req) {
        User me = requireStudent();
        studentResumeBusinessService.upsertMine(me.getId(), req);
        return ResponseEntity.ok(ApiResponse.success("已保存", null));
    }

    @Operation(summary = "上传简历 PDF 附件")
    @PostMapping("/attachment")
    public ResponseEntity<ApiResponse<StudentResumeVO>> uploadAttachment(@RequestPart("file") MultipartFile file) {
        User me = requireStudent();
        return ResponseEntity.ok(ApiResponse.success(studentResumeBusinessService.saveAttachment(me.getId(), file)));
    }
}
