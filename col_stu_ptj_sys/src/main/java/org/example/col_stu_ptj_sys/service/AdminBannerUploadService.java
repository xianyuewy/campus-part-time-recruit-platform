package org.example.col_stu_ptj_sys.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.config.UploadPathService;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.springframework.stereotype.Service;
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

/**
 * 管理端首页轮播图上传（存本地 uploads/banners，返回可访问路径）。
 */
@Slf4j
@Service
public class AdminBannerUploadService {

    private static final Set<String> ALLOWED_EXT = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private final UploadPathService uploadPathService;

    public AdminBannerUploadService(UploadPathService uploadPathService) {
        this.uploadPathService = uploadPathService;
    }

    @PostConstruct
    public void ensureDir() throws IOException {
        Files.createDirectories(uploadPathService.resolve("banners"));
    }

    public String saveBanner(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择图片文件");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("轮播图单张不能超过 5MB");
        }
        String orig = Objects.requireNonNullElse(file.getOriginalFilename(), "banner");
        String ext = extOf(orig).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg/png/gif/webp");
        }
        String ct = file.getContentType();
        if (!isAllowedImageContentType(ct, ext)) {
            throw new BusinessException("文件类型需为图片");
        }
        String name = "banner_" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = uploadPathService.resolve("banners");
        Path target = dir.resolve(name);
        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("保存轮播图失败: {}", e.getMessage(), e);
            throw new BusinessException("保存图片失败");
        }
        return "/uploads/banners/" + name;
    }

    private static String extOf(String filename) {
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i) : ".jpg";
    }

    private static boolean isAllowedImageContentType(String contentType, String extLower) {
        if (!StringUtils.hasText(contentType)) {
            return true;
        }
        String c = contentType.trim().toLowerCase(Locale.ROOT);
        if (c.startsWith("image/")) {
            return true;
        }
        return "application/octet-stream".equals(c) && ALLOWED_EXT.contains(extLower);
    }
}
