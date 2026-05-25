package org.example.col_stu_ptj_sys.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.service.JwtService;
import org.example.col_stu_ptj_sys.service.UserService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 负责从HTTP请求头中提取JWT令牌并进行验证
 * 验证通过后，将用户认证信息设置到SecurityContext中[2,4](@ref)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    /**
     * 核心过滤方法，处理每个HTTP请求[2,4](@ref)
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        log.debug("开始处理JWT认证过滤器: {}", request.getRequestURI());
        
        // 1. 从请求头中获取Authorization头[2,4](@ref)
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        
        // 2. 检查Authorization头是否存在且格式正确[2](@ref)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("请求未包含Bearer token，继续过滤器链");
            filterChain.doFilter(request, response);
            return;
        }
        
        // 3. 提取JWT令牌（去除"Bearer "前缀）[2](@ref)
        jwt = authHeader.substring(7);
        log.debug("提取到JWT令牌，长度: {}", jwt.length());
        
        try {
            // 4. 从JWT中提取用户名[2](@ref)
            username = jwtService.extractUsername(jwt);
            log.debug("从JWT中提取到用户名: {}", username);
            
            // 5. 如果用户名不为空且当前SecurityContext中没有认证信息[2,4](@ref)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("开始验证用户: {}", username);

                User userEntity = userService.findByUsername(username)
                        .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("用户不存在"));
                int dbTv = userEntity.getTokenVersion() != null ? userEntity.getTokenVersion() : 0;
                int jwtTv = jwtService.extractTokenVersion(jwt);
                if (dbTv != jwtTv) {
                    log.warn("JWT 令牌版本失效: {}, dbTv={}, jwtTv={}", username, dbTv, jwtTv);
                    handleJwtException(response, "登录状态已失效，请重新登录", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // 6. 从数据库加载用户详细信息[2](@ref)
                UserDetails userDetails = userService.loadUserByUsername(username);
                log.debug("成功加载用户详情: {}", username);

                // 6.1 账号停用则拒绝访问
                if (!userDetails.isEnabled()) {
                    log.warn("账号已禁用: {}", username);
                    handleJwtException(response, "账号已禁用", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                // 7. 验证JWT令牌是否有效[4](@ref)
                if (jwtService.validateToken(jwt, userDetails)) {
                    log.debug("JWT令牌验证成功，创建认证令牌");
                    
                    // 8. 创建认证令牌并设置到SecurityContext中[2,4](@ref)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // 证书信息，JWT不需要密码
                            userDetails.getAuthorities()
                    );
                    
                    // 9. 添加请求详情信息[2](@ref)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 10. 将认证信息设置到SecurityContext中[2,4](@ref)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("用户 {} 认证成功，请求路径: {}", username, request.getRequestURI());
                    
                } else {
                    log.warn("JWT令牌验证失败: {}", username);
                }
            }
            
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
            handleJwtException(response, "令牌已过期", HttpServletResponse.SC_UNAUTHORIZED);
            return;
            
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("JWT令牌格式错误: {}", e.getMessage());
            handleJwtException(response, "令牌格式错误", HttpServletResponse.SC_BAD_REQUEST);
            return;
            
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("JWT签名验证失败: {}", e.getMessage());
            handleJwtException(response, "令牌签名无效", HttpServletResponse.SC_UNAUTHORIZED);
            return;
            
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            log.warn("用户不存在: {}", e.getMessage());
            handleJwtException(response, "用户不存在", HttpServletResponse.SC_UNAUTHORIZED);
            return;

        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("JWT 校验失败: {}", e.getMessage());
            handleJwtException(response, "令牌无效或已失效", HttpServletResponse.SC_UNAUTHORIZED);
            return;

        } catch (org.springframework.dao.DataAccessException e) {
            Throwable t = e.getMostSpecificCause();
            String detail = t != null ? t.getMessage() : e.getMessage();
            log.error("JWT 认证阶段数据访问失败: {}", detail, e);
            if (detail != null && detail.contains("token_version")) {
                handleJwtException(response,
                        "数据库缺少 token_version 列，请执行 db/schema-token-version.sql 后重启",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                handleJwtException(response, "认证系统错误", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;

        } catch (Exception e) {
            log.error("JWT认证过程中发生未知错误: {}", e.getMessage(), e);
            handleJwtException(response, "认证系统错误", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // 11. 继续过滤器链[2,4](@ref)
        filterChain.doFilter(request, response);
    }
    
    /**
     * 处理JWT认证异常，返回标准的错误响应[3](@ref)
     */
    private void handleJwtException(HttpServletResponse response, String message, int statusCode) 
            throws IOException {
        
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        
        String errorResponse = String.format(
            "{\"success\":false,\"code\":%d,\"message\":\"%s\",\"timestamp\":\"%s\"}",
            statusCode,
            message,
            java.time.LocalDateTime.now()
        );
        
        response.getWriter().write(errorResponse);
        response.getWriter().flush();
    }
    
    /**
     * 配置不需要JWT认证的路径[4](@ref)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 以下路径不需要JWT认证[4](@ref)
        boolean shouldNotFilter = path.startsWith("/api/auth/") && 
                                !path.equals("/api/auth/me") && // 获取当前用户信息需要认证
                                !path.equals("/api/auth/logout"); // 登出需要认证
        
        if (shouldNotFilter) {
            log.debug("路径 {} 跳过JWT认证", path);
        }
        
        return shouldNotFilter;
    }
}