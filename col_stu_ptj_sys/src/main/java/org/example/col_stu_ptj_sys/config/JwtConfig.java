package org.example.col_stu_ptj_sys.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secretKey;
    private long accessTokenExpiration;    // 15分钟
    private long refreshTokenExpiration; // 7天
}