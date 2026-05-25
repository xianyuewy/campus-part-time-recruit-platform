package org.example.col_stu_ptj_sys.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;

import java.time.LocalDateTime;

/**
 * 认证响应数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 不序列化为null的字段
public class AuthResponse {
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名 / 企业可填企业名称
     */
    private String realName;

    /**
     * 全站外显名（企业优先企业资料中的名称，否则昵称，否则登录名）
     */
    private String displayName;
    
    /**
     * 用户角色
     */
    private UserRole role;
    
    /**
     * 认证状态
     */
    private AuthStatus authStatus;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 访问令牌过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accessTokenExpires;
    
    /**
     * 刷新令牌过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refreshTokenExpires;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 成功标志
     */
    private Boolean success;
    
    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * 创建失败响应
     */
    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建登出响应
     */
    public static AuthResponse logoutSuccess() {
        return AuthResponse.builder()
                .success(true)
                .message("登出成功")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建刷新令牌响应
     */
    public static AuthResponse refreshSuccess(String newAccessToken, LocalDateTime accessExpires) {
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .accessTokenExpires(accessExpires)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }
}