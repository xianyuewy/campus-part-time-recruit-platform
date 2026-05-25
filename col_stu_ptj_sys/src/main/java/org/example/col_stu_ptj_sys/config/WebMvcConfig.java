package org.example.col_stu_ptj_sys.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 本地文件上传目录映射为 /uploads/**
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UploadPathService uploadPathService;

    public WebMvcConfig(UploadPathService uploadPathService) {
        this.uploadPathService = uploadPathService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPathService.uploadRoot().toUri().toString());
    }
}
