package org.example.col_stu_ptj_sys.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.dto.chat.ApplicationMessageVO;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 按投递 ID 分房间广播聊天消息。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationChatWebSocketHub {

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_APPLICATION_ID = "applicationId";

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    public void join(long applicationId, WebSocketSession session) {
        rooms.computeIfAbsent(applicationId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void leave(WebSocketSession session) {
        Long appId = (Long) session.getAttributes().get(ATTR_APPLICATION_ID);
        if (appId == null) {
            return;
        }
        Set<WebSocketSession> set = rooms.get(appId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                rooms.remove(appId, set);
            }
        }
    }

    public void broadcastNewMessage(long applicationId, ApplicationMessageVO vo) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "type", "NEW_MESSAGE",
                    "message", vo));
        } catch (Exception e) {
            log.warn("序列化聊天推送失败: {}", e.getMessage());
            return;
        }
        TextMessage tm = new TextMessage(payload);
        Set<WebSocketSession> set = rooms.get(applicationId);
        if (set == null || set.isEmpty()) {
            return;
        }
        for (WebSocketSession s : set) {
            if (!s.isOpen()) {
                continue;
            }
            try {
                synchronized (s) {
                    s.sendMessage(tm);
                }
            } catch (IOException e) {
                log.debug("推送 WebSocket 失败，将移除会话: {}", e.getMessage());
                leave(s);
            }
        }
    }
}
