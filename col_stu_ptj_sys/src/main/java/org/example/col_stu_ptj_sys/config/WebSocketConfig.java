package org.example.col_stu_ptj_sys.config;

import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.websocket.ApplicationChatHandshakeInterceptor;
import org.example.col_stu_ptj_sys.websocket.ApplicationChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ApplicationChatWebSocketHandler applicationChatWebSocketHandler;
    private final ApplicationChatHandshakeInterceptor applicationChatHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(applicationChatWebSocketHandler, "/ws/application-chat")
                .addInterceptors(applicationChatHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
