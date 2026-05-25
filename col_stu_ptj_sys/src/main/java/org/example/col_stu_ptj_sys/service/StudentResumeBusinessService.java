package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.config.UploadPathService;
import org.example.col_stu_ptj_sys.dto.student.StudentResumeVO;
import org.example.col_stu_ptj_sys.dto.student.UpsertStudentResumeRequest;
import org.example.col_stu_ptj_sys.entity.JobApplication;
import org.example.col_stu_ptj_sys.entity.StudentResume;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.mapper.StudentResumeMapper;
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
public class StudentResumeBusinessService {

    private static final Set<String> ALLOWED_RESUME_EXT = Set.of(".pdf");
    private static final long MAX_RESUME_BYTES = 15 * 1024 * 1024;

    private final StudentResumeMapper studentResumeMapper;
    private final JobApplicationService jobApplicationService;
    private final UploadPathService uploadPathService;

    @PostConstruct
    public void ensureResumeUploadDir() throws IOException {
        Files.createDirectories(uploadPathService.resolve("resumes"));
    }

    public boolean hasResume(Long userId) {
        if (userId == null) {
            return false;
        }
        StudentResume r = studentResumeMapper.selectOne(
                new LambdaQueryWrapper<StudentResume>().eq(StudentResume::getUserId, userId));
        if (r == null) {
            return false;
        }
        return StringUtils.hasText(r.getSelfIntro())
                || StringUtils.hasText(r.getEducation())
                || StringUtils.hasText(r.getSkills())
                || StringUtils.hasText(r.getWorkExperience())
                || StringUtils.hasText(r.getAttachmentPath());
    }

    public StudentResumeVO getMine(Long studentUserId) {
        StudentResume r = studentResumeMapper.selectOne(
                new LambdaQueryWrapper<StudentResume>().eq(StudentResume::getUserId, studentUserId));
        return toVo(r, studentUserId);
    }

    @Transactional
    public void upsertMine(Long studentUserId, UpsertStudentResumeRequest req) {
        StudentResume exist = studentResumeMapper.selectOne(
                new LambdaQueryWrapper<StudentResume>().eq(StudentResume::getUserId, studentUserId));
        if (exist == null) {
            StudentResume n = new StudentResume();
            n.setUserId(studentUserId);
            fill(n, req);
            studentResumeMapper.insert(n);
        } else {
            fill(exist, req);
            studentResumeMapper.updateById(exist);
        }
    }

    /**
     * 上传或替换简历 PDF 附件（仅 PDF，最大 15MB）。
     */
    @Transactional
    public StudentResumeVO saveAttachment(Long studentUserId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择 PDF 文件");
        }
        if (file.getSize() > MAX_RESUME_BYTES) {
            throw new BusinessException("简历附件不能超过 15MB");
        }
        String orig = Objects.requireNonNullElse(file.getOriginalFilename(), "resume.pdf");
        String ext = extOf(orig).toLowerCase(Locale.ROOT);
        if (!ALLOWED_RESUME_EXT.contains(ext)) {
            throw new BusinessException("仅支持 PDF 格式");
        }
        String ct = file.getContentType();
        if (ct != null && !ct.isBlank() && !ct.equalsIgnoreCase("application/pdf")) {
            throw new BusinessException("文件类型需为 PDF");
        }
        String safeName = sanitizeOriginalName(orig);
        String stored = studentUserId + "_" + UUID.randomUUID().toString().replace("-", "") + ".pdf";
        Path dir = uploadPathService.resolve("resumes");
        Path target = dir.resolve(stored);
        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("保存简历附件失败: {}", e.getMessage(), e);
            throw new BusinessException("保存附件失败");
        }
        String urlPath = "/uploads/resumes/" + stored;

        StudentResume exist = studentResumeMapper.selectOne(
                new LambdaQueryWrapper<StudentResume>().eq(StudentResume::getUserId, studentUserId));
        if (exist == null) {
            StudentResume n = new StudentResume();
            n.setUserId(studentUserId);
            n.setAttachmentPath(urlPath);
            n.setAttachmentOriginalName(safeName);
            studentResumeMapper.insert(n);
        } else {
            deleteStoredFile(exist.getAttachmentPath());
            exist.setAttachmentPath(urlPath);
            exist.setAttachmentOriginalName(safeName);
            studentResumeMapper.updateById(exist);
        }
        return getMine(studentUserId);
    }

    private static String sanitizeOriginalName(String name) {
        String base = name.replace('\\', '/');
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        if (base.length() > 200) {
            base = base.substring(0, 200);
        }
        return base.isBlank() ? "resume.pdf" : base;
    }

    private void deleteStoredFile(String path) {
        if (!StringUtils.hasText(path) || !path.startsWith("/uploads/resumes/")) {
            return;
        }
        String name = path.substring("/uploads/resumes/".length());
        if (name.contains("..") || name.contains("/") || name.contains("\\")) {
            return;
        }
        try {
            Path root = uploadPathService.resolve("resumes");
            Path p = root.resolve(name).normalize();
            if (!p.startsWith(root)) {
                return;
            }
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // ignore
        }
    }

    private static String extOf(String filename) {
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i) : "";
    }

    public StudentResumeVO getForCompanyApplication(Long applicationId, Long companyUserId) {
        JobApplication app = jobApplicationService.getById(applicationId);
        if (app == null || !companyUserId.equals(app.getCompanyUserId())) {
            throw new BusinessException(404, "投递不存在");
        }
        StudentResume r = studentResumeMapper.selectOne(
                new LambdaQueryWrapper<StudentResume>().eq(StudentResume::getUserId, app.getStudentUserId()));
        return toVo(r, app.getStudentUserId());
    }

    private static void fill(StudentResume target, UpsertStudentResumeRequest req) {
        target.setSelfIntro(trimToNull(req.getSelfIntro()));
        target.setEducation(trimToNull(req.getEducation()));
        target.setSkills(trimToNull(req.getSkills()));
        target.setWorkExperience(trimToNull(req.getWorkExperience()));
    }

    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private static StudentResumeVO toVo(StudentResume r, Long userId) {
        if (r == null) {
            return StudentResumeVO.builder()
                    .userId(userId)
                    .selfIntro(null)
                    .education(null)
                    .skills(null)
                    .workExperience(null)
                    .attachmentPath(null)
                    .attachmentOriginalName(null)
                    .updateTime(null)
                    .build();
        }
        return StudentResumeVO.builder()
                .userId(r.getUserId())
                .selfIntro(r.getSelfIntro())
                .education(r.getEducation())
                .skills(r.getSkills())
                .workExperience(r.getWorkExperience())
                .attachmentPath(r.getAttachmentPath())
                .attachmentOriginalName(r.getAttachmentOriginalName())
                .updateTime(r.getUpdateTime())
                .build();
    }
}
