package com.ecommerce.websocket;

import com.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CartWebSocketHandler extends TextWebSocketHandler {

    private final CartService cartService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public CartWebSocketHandler(CartService cartService, ObjectMapper objectMapper) {
        this.cartService = cartService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Long cartId = Long.valueOf(payload.get("cartId").toString());
        
        String action = payload.get("action").toString();
        
        if ("update".equals(action)) {
            Long itemId = Long.valueOf(payload.get("itemId").toString());
            int quantity = Integer.parseInt(payload.get("quantity").toString());
            cartService.updateItemQuantity(cartId, itemId, quantity);
            
            for (WebSocketSession s : sessions.values()) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    Map.of("type", "cart_updated", "cartId", cartId)
                )));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    }
}