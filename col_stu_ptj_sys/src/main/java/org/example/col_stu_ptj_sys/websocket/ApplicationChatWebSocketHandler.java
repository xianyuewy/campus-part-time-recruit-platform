package org.example.col_stu_ptj_sys.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.service.ApplicationChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationChatWebSocketHandler extends TextWebSocketHandler {

    private final ApplicationChatService applicationChatService;
    private final ApplicationChatWebSocketHub hub;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long applicationId = (Long) session.getAttributes().get(ApplicationChatWebSocketHub.ATTR_APPLICATION_ID);
        if (applicationId != null) {
            hub.join(applicationId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get(ApplicationChatWebSocketHub.ATTR_USER_ID);
        Long applicationId = (Long) session.getAttributes().get(ApplicationChatWebSocketHub.ATTR_APPLICATION_ID);
        if (userId == null || applicationId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(message.getPayload());
        } catch (Exception e) {
            sendError(session, "消息格式错误");
            return;
        }
        JsonNode contentNode = root.get("content");
        if (contentNode == null || !contentNode.isTextual()) {
            sendError(session, "缺少 content");
            return;
        }
        String content = contentNode.asText();
        try {
            applicationChatService.sendMessage(applicationId, userId, content);
        } catch (BusinessException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            log.warn("处理聊天消息失败: {}", e.getMessage());
            sendError(session, "发送失败");
        }
    }

    private void sendError(WebSocketSession session, String msg) throws Exception {
        if (!session.isOpen()) {
            return;
        }
        String json = objectMapper.writeValueAsString(Map.of("type", "ERROR", "message", msg));
        session.sendMessage(new TextMessage(json));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        hub.leave(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        hub.leave(session);
    }
}
