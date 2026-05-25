package org.example.col_stu_ptj_sys.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.ChangePasswordRequest;
import org.example.col_stu_ptj_sys.dto.HomeSummaryVO;
import org.example.col_stu_ptj_sys.dto.UpdateProfileRequest;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.service.HomeSummaryService;
import org.example.col_stu_ptj_sys.service.UserProfileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "个人资料", description = "基本信息、头像、密码（学生/企业/管理员共用）")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final HomeSummaryService homeSummaryService;

    @Operation(summary = "首页欢迎区统计（按角色）")
    @GetMapping("/home-summary")
    public ResponseEntity<ApiResponse<HomeSummaryVO>> homeSummary() {
        return ResponseEntity.ok(ApiResponse.success(homeSummaryService.summary()));
    }

    @Operation(summary = "更新基本信息（邮箱、手机）")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        userProfileService.updateProfile(req);
        return ResponseEntity.ok(ApiResponse.success("已保存", null));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userProfileService.changePassword(req);
        return ResponseEntity.ok(ApiResponse.success("密码已更新", null));
    }

    @Operation(summary = "上传头像")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AvatarPathVO>> uploadAvatar(@RequestPart("file") MultipartFile file) {
        String path = userProfileService.saveAvatar(file);
        AvatarPathVO vo = new AvatarPathVO();
        vo.setAvatar(path);
        return ResponseEntity.ok(ApiResponse.success("上传成功", vo));
    }

    @Operation(summary = "上传学生资质材料（学信网/学生证）")
    @PostMapping(value = "/student/qualification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StudentQualificationVO>> uploadStudentQualification(@RequestPart("file") MultipartFile file) {
        String path = userProfileService.saveStudentQualification(file);
        StudentQualificationVO vo = new StudentQualificationVO();
        vo.setIdCard(path);
        User me = userProfileService.getCurrentUserProfile();
        vo.setAuthStatus(me.getAuthStatus());
        vo.setAuthRemark(me.getAuthRemark());
        vo.setAuthTime(me.getAuthTime() != null ? me.getAuthTime().toString() : null);
        return ResponseEntity.ok(ApiResponse.success("上传成功", vo));
    }

    @Operation(summary = "学生资质信息")
    @GetMapping("/student/qualification")
    public ResponseEntity<ApiResponse<StudentQualificationVO>> getStudentQualification() {
        User me = userProfileService.getCurrentUserProfile();
        StudentQualificationVO vo = new StudentQualificationVO();
        vo.setIdCard(me.getIdCard());
        vo.setAuthStatus(me.getAuthStatus());
        vo.setAuthRemark(me.getAuthRemark());
        vo.setAuthTime(me.getAuthTime() != null ? me.getAuthTime().toString() : null);
        return ResponseEntity.ok(ApiResponse.success(vo));
    }

    @Operation(summary = "提交学生资质审核")
    @PostMapping("/student/submit-audit")
    public ResponseEntity<ApiResponse<Void>> submitStudentAudit() {
        userProfileService.submitStudentQualificationAudit();
        return ResponseEntity.ok(ApiResponse.success("提交成功，请等待管理员审核", null));
    }

    @Data
    public static class StudentQualificationVO {
        private String idCard;
        private AuthStatus authStatus;
        private String authRemark;
        private String authTime;
    }

    @Data
    public static class AvatarPathVO {
        private String avatar;
    }
}
