package org.example.col_stu_ptj_sys.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadPathService {

    private final Path uploadRoot;

    public UploadPathService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Path uploadRoot() {
        return uploadRoot;
    }

    public Path resolve(String... parts) {
        Path p = uploadRoot;
        for (String part : parts) {
            p = p.resolve(part);
        }
        return p.normalize();
    }
}
