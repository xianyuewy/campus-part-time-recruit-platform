package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.dto.LoginRequest;
import org.example.col_stu_ptj_sys.dto.RegisterRequest;
import org.example.col_stu_ptj_sys.dto.AuthResponse;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserCreditService userCreditService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDisplayService userDisplayService;

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("开始用户注册流程: {}", request.getUsername());

        // 1. 检查用户名是否已存在
        if (userService.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 2. 检查邮箱是否已存在
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 3. 验证角色参数
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("角色参数无效，必须是STUDENT或COMPANY");
        }

        // 4. 创建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setAuthStatus(AuthStatus.UNAUTH);
        user.setAvatar("/avatars/default.png");
        user.setTokenVersion(0);

        // 5. 保存用户
        userService.save(user);
        log.info("用户保存成功: ID={}", user.getId());

        userCreditService.initForNewUser(user.getId());

        // 6. 生成Token
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 7. 计算Token过期时间
        LocalDateTime accessExpires = LocalDateTime.now().plusSeconds(
                jwtService.getAccessTokenExpiration() / 1000
        );
        LocalDateTime refreshExpires = LocalDateTime.now().plusSeconds(
                jwtService.getRefreshTokenExpiration() / 1000
        );

        // 8. 构建响应
        return buildTokenResponse(user, accessToken, refreshToken, accessExpires, refreshExpires);
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        log.debug("开始用户登录流程: {}", request.getUsername());

        try {
            // 1. 认证用户
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. 获取用户信息
            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 3. 检查用户状态
            if (user.getAuthStatus() == AuthStatus.REJECTED) {
                throw new RuntimeException("账号认证失败，请联系管理员");
            }

            // 4. 生成Token
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // 5. 计算Token过期时间
            LocalDateTime accessExpires = LocalDateTime.now().plusSeconds(
                    jwtService.getAccessTokenExpiration() / 1000
            );
            LocalDateTime refreshExpires = LocalDateTime.now().plusSeconds(
                    jwtService.getRefreshTokenExpiration() / 1000
            );

            // 6. 构建响应
            AuthResponse response = buildTokenResponse(user, accessToken, refreshToken, accessExpires, refreshExpires);

            log.info("用户登录成功: username={}", user.getUsername());
            return response;

        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("用户认证失败: username={}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
    }

    /**
     * 刷新Token
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("开始刷新Token流程");

        try {
            // 1. 验证Refresh Token并提取用户名
            String username = jwtService.extractUsername(refreshToken);
            log.debug("从Refresh Token中提取用户名: {}", username);

            // 2. 获取用户信息
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 3. 验证Token（使用User实体）
            if (!jwtService.validateToken(refreshToken, user)) {
                log.warn("Refresh Token验证失败: username={}", username);
                throw new RuntimeException("刷新令牌无效");
            }
            int jwtTv = jwtService.extractTokenVersion(refreshToken);
            int dbTv = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
            if (jwtTv != dbTv) {
                log.warn("Refresh Token 版本失效: username={}, jwtTv={}, dbTv={}", username, jwtTv, dbTv);
                throw new RuntimeException("刷新令牌已失效，请重新登录");
            }

            // 4. 生成新的Token
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            log.debug("生成新的Access Token和Refresh Token");

            // 5. 计算新的过期时间
            LocalDateTime accessExpires = LocalDateTime.now().plusSeconds(
                    jwtService.getAccessTokenExpiration() / 1000
            );
            LocalDateTime refreshExpires = LocalDateTime.now().plusSeconds(
                    jwtService.getRefreshTokenExpiration() / 1000
            );

            // 6. 构建响应
            AuthResponse response = buildTokenResponse(user, newAccessToken, newRefreshToken, accessExpires, refreshExpires);

            log.info("Token刷新成功: username={}", username);
            return response;

        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            throw new RuntimeException("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    public AuthResponse getCurrentUser() {
        try {
            // 1. 从SecurityContext获取认证信息
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("用户未认证");
                return null;
            }

            // 2. 获取用户名
            String username = authentication.getName();
            log.debug("获取当前用户: {}", username);

            // 3. 获取用户信息
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 4. 构建响应（不包含Token）
            AuthResponse response = AuthResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .realName(user.getRealName())
                    .displayName(userDisplayService.publicDisplayName(user))
                    .role(user.getRole())
                    .authStatus(user.getAuthStatus())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.debug("成功获取当前用户信息: {}", username);
            return response;

        } catch (Exception e) {
            log.error("获取当前用户信息失败: {}", e.getMessage());
            return null;
        }
    }

    private AuthResponse buildTokenResponse(User user, String accessToken, String refreshToken,
                                            LocalDateTime accessExpires, LocalDateTime refreshExpires) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .displayName(userDisplayService.publicDisplayName(user))
                .role(user.getRole())
                .authStatus(user.getAuthStatus())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .accessTokenExpires(accessExpires)
                .refreshTokenExpires(refreshExpires)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }
}