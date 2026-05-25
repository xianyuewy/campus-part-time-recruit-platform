package org.example.col_stu_ptj_sys.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.AuthResponse;
import org.example.col_stu_ptj_sys.dto.LoginRequest;
import org.example.col_stu_ptj_sys.dto.RegisterRequest;
import org.example.col_stu_ptj_sys.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        try {
            log.info("用户注册请求: username={}, email={}, role={}",
                    request.getUsername(), request.getEmail(), request.getRole());

            // 调用认证服务进行注册
            AuthResponse authResponse = authService.register(request);

            // 设置Refresh Token到HttpOnly Cookie中
            setRefreshTokenCookie(response, authResponse.getRefreshToken());

            log.info("用户注册成功: username={}, userId={}",
                    authResponse.getUsername(), authResponse.getUserId());

            return ResponseEntity.ok(ApiResponse.success("注册成功", authResponse));

        } catch (IllegalArgumentException e) {
            log.warn("注册参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));

        } catch (RuntimeException e) {
            log.error("注册业务异常: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));

        } catch (Exception e) {
            log.error("注册系统异常: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "系统繁忙，请稍后重试"));
        }
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        try {
            log.info("用户登录请求: username={}", request.getUsername());

            // 调用认证服务进行登录
            AuthResponse authResponse = authService.login(request);

            // 设置Refresh Token到HttpOnly Cookie中
            setRefreshTokenCookie(response, authResponse.getRefreshToken());

            log.info("用户登录成功: username={}, userId={}",
                    authResponse.getUsername(), authResponse.getUserId());

            return ResponseEntity.ok(ApiResponse.success("登录成功", authResponse));

        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("登录认证失败: username={}, reason={}",
                    request.getUsername(), e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "用户名或密码错误"));

        } catch (RuntimeException e) {
            log.error("登录业务异常: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));

        } catch (Exception e) {
            log.error("登录系统异常: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "系统繁忙，请稍后重试"));
        }
    }

    /**
     * 刷新Token接口
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error(401, "刷新令牌不存在"));
            }

            log.info("Token刷新请求");
            AuthResponse authResponse = authService.refreshToken(refreshToken);

            // 更新Cookie中的Refresh Token
            setRefreshTokenCookie(response, authResponse.getRefreshToken());

            log.info("Token刷新成功: username={}", authResponse.getUsername());
            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", authResponse));

        } catch (RuntimeException e) {
            log.warn("Token刷新失败: {}", e.getMessage());
            return ResponseEntity.status(401).body(ApiResponse.error(401, e.getMessage()));

        } catch (Exception e) {
            log.error("Token刷新系统异常: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "系统繁忙，请稍后重试"));
        }
    }

    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<AuthResponse>> logout(HttpServletResponse response) {
        try {
            log.info("用户登出请求");

            // 清除Refresh Token Cookie
            clearRefreshTokenCookie(response);

            log.info("用户登出成功");
            return ResponseEntity.ok(ApiResponse.success(AuthResponse.logoutSuccess()));

        } catch (Exception e) {
            log.error("登出系统异常: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "系统繁忙，请稍后重试"));
        }
    }

    /**
     * 获取当前用户信息接口
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser() {
        try {
            // 从SecurityContext中获取当前用户信息
            AuthResponse currentUser = authService.getCurrentUser();

            if (currentUser == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error(401, "用户未登录"));
            }

            return ResponseEntity.ok(ApiResponse.success("获取成功", currentUser));

        } catch (Exception e) {
            log.error("获取用户信息异常: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "系统繁忙，请稍后重试"));
        }
    }

    /**
     * 设置Refresh Token到HttpOnly Cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return;
        }

        // 创建安全的HttpOnly Cookie
        String cookieValue = String.format(
                "refreshToken=%s; Path=/; HttpOnly; SameSite=Strict; Max-Age=%d",
                refreshToken,
                TimeUnit.DAYS.toSeconds(7) // 7天有效期
        );

        // 如果是生产环境，添加Secure标志（仅HTTPS）
        // cookieValue += "; Secure";

        response.addHeader("Set-Cookie", cookieValue);
        log.debug("已设置Refresh Token Cookie");
    }

    /**
     * 清除Refresh Token Cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        String cookieValue = "refreshToken=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0";
        response.addHeader("Set-Cookie", cookieValue);
        log.debug("已清除Refresh Token Cookie");
    }
}