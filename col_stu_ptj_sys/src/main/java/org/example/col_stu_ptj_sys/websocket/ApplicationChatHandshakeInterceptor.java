package org.example.col_stu_ptj_sys.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.service.ApplicationChatService;
import org.example.col_stu_ptj_sys.service.JwtService;
import org.example.col_stu_ptj_sys.service.UserService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手：query 参数 token、applicationId；校验 JWT 与投递参与方。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserService userService;
    private final ApplicationChatService applicationChatService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                     WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (!(request instanceof ServletServerHttpRequest servletReq)) {
            return false;
        }
        var req = servletReq.getServletRequest();
        String token = req.getParameter("token");
        String appIdStr = req.getParameter("applicationId");
        if (token == null || token.isBlank() || appIdStr == null || appIdStr.isBlank()) {
            log.warn("WebSocket 握手缺少 token 或 applicationId");
            return false;
        }
        long applicationId;
        try {
            applicationId = Long.parseLong(appIdStr.trim());
        } catch (NumberFormatException e) {
            return false;
        }
        try {
            String username = jwtService.extractUsername(token);
            User user = userService.findByUsername(username).orElse(null);
            if (user == null) {
                return false;
            }
            int dbTv = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
            int jwtTv = jwtService.extractTokenVersion(token);
            if (dbTv != jwtTv) {
                log.warn("WebSocket 令牌版本失效: {}", username);
                return false;
            }
            UserDetails userDetails = userService.loadUserByUsername(username);
            if (!userDetails.isEnabled()) {
                return false;
            }
            if (!jwtService.validateToken(token, userDetails)) {
                return false;
            }
            long userId = user.getId();
            applicationChatService.assertParticipant(applicationId, userId);
            attributes.put(ApplicationChatWebSocketHub.ATTR_USER_ID, userId);
            attributes.put(ApplicationChatWebSocketHub.ATTR_APPLICATION_ID, applicationId);
            return true;
        } catch (BusinessException e) {
            log.debug("WebSocket 握手业务拒绝: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.debug("WebSocket 握手失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
