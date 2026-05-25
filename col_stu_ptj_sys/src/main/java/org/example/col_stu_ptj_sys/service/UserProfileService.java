package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.config.UploadPathService;
import org.example.col_stu_ptj_sys.dto.ChangePasswordRequest;
import org.example.col_stu_ptj_sys.dto.UpdateProfileRequest;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final Set<String> ALLOWED_IMG_EXT = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final long MAX_AVATAR_BYTES = 10 * 1024 * 1024;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UploadPathService uploadPathService;

    @PostConstruct
    public void ensureUploadDirs() throws IOException {
        Files.createDirectories(uploadPathService.resolve("avatars"));
        Files.createDirectories(uploadPathService.resolve("dispute"));
        Files.createDirectories(uploadPathService.resolve("student-auth"));
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Transactional
    public void updateProfile(UpdateProfileRequest req) {
        User me = currentUser();
        if (StringUtils.hasText(req.getEmail())) {
            String em = req.getEmail().trim();
            if (!em.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                throw new BusinessException("邮箱格式不正确");
            }
            User other = userService.getOne(new QueryWrapper<User>()
                    .eq("email", em)
                    .ne("id", me.getId())
                    .last("LIMIT 1"));
            if (other != null) {
                throw new BusinessException("该邮箱已被其他账号使用");
            }
            me.setEmail(em);
        }
        if (req.getPhone() != null) {
            me.setPhone(StringUtils.hasText(req.getPhone()) ? req.getPhone().trim() : null);
        }
        if (req.getNickname() != null) {
            me.setNickname(StringUtils.hasText(req.getNickname()) ? req.getNickname().trim() : null);
        }
        if (req.getRealName() != null) {
            me.setRealName(StringUtils.hasText(req.getRealName()) ? req.getRealName().trim() : null);
        }
        userService.updateById(me);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        User me = currentUser();
        if (!passwordEncoder.matches(req.getOldPassword(), me.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        me.setPassword(passwordEncoder.encode(req.getNewPassword()));
        int v = me.getTokenVersion() != null ? me.getTokenVersion() : 0;
        me.setTokenVersion(v + 1);
        userService.updateById(me);
    }

    @Transactional
    public String saveStudentQualification(MultipartFile file) {
        User me = currentUser();
        if (me.getRole() != UserRole.STUDENT) {
            throw new BusinessException(403, "仅学生可上传资质材料");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择资质材料文件");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new BusinessException("资质材料不能超过 10MB");
        }
        String orig = Objects.requireNonNullElse(file.getOriginalFilename(), "student_auth");
        String ext = extOf(orig).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMG_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg/png/gif/webp");
        }
        String ct = file.getContentType();
        if (!isAllowedImageContentType(ct, ext)) {
            throw new BusinessException("仅支持图片格式");
        }
        String name = me.getId() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = uploadPathService.resolve("student-auth");
        Path target = dir.resolve(name);
        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("保存学生资质材料失败: {}", e.getMessage(), e);
            throw new BusinessException("保存资质材料失败");
        }
        String url = "/uploads/student-auth/" + name;
        me.setIdCard(url);
        // 重新上传后回到未认证，需重新提交审核
        me.setAuthStatus(AuthStatus.UNAUTH);
        me.setAuthRemark(null);
        me.setAuthTime(null);
        userService.updateById(me);
        return url;
    }

    @Transactional
    public void submitStudentQualificationAudit() {
        User me = currentUser();
        if (me.getRole() != UserRole.STUDENT) {
            throw new BusinessException(403, "仅学生可提交资质审核");
        }
        if (!StringUtils.hasText(me.getIdCard())) {
            throw new BusinessException("请先上传学信网或学生证材料");
        }
        if (me.getAuthStatus() == AuthStatus.PENDING) {
            throw new BusinessException("你已提交审核，请耐心等待");
        }
        if (me.getAuthStatus() == AuthStatus.APPROVED) {
            throw new BusinessException("已通过认证，无需重复提交");
        }
        me.setAuthStatus(AuthStatus.PENDING);
        me.setAuthRemark(null);
        me.setAuthTime(null);
        userService.updateById(me);
    }

    public User getCurrentUserProfile() {
        return currentUser();
    }

    /**
     * 保存头像文件并更新用户 avatar 字段为可访问路径（如 /uploads/avatars/xxx.png）
     */
    @Transactional
    public String saveAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择图片文件");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new BusinessException("头像文件不能超过 10MB");
        }
        String orig = Objects.requireNonNullElse(file.getOriginalFilename(), "avatar");
        String ext = extOf(orig).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMG_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg/png/gif/webp");
        }
        String ct = file.getContentType();
        if (!isAllowedImageContentType(ct, ext)) {
            throw new BusinessException("仅支持图片格式");
        }
        User me = currentUser();
        String name = me.getId() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = uploadPathService.resolve("avatars");
        Path target = dir.resolve(name);
        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("保存头像失败: {}", e.getMessage(), e);
            throw new BusinessException("保存头像失败");
        }
        String url = "/uploads/avatars/" + name;
        me.setAvatar(url);
        userService.updateById(me);
        return url;
    }

    private static String extOf(String filename) {
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i) : ".png";
    }

    /** 浏览器常把本地图片标成 octet-stream 或省略 Content-Type，扩展名已校验时再按类型放行 */
    private static boolean isAllowedImageContentType(String contentType, String extLower) {
        if (!StringUtils.hasText(contentType)) {
            return true;
        }
        String c = contentType.trim().toLowerCase(Locale.ROOT);
        if (c.startsWith("image/")) {
            return true;
        }
        if ("application/octet-stream".equals(c)) {
            return ALLOWED_IMG_EXT.contains(extLower);
        }
        return false;
    }
}
