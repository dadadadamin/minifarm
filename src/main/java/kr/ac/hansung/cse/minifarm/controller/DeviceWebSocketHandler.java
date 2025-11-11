package kr.ac.hansung.cse.minifarm.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.hansung.cse.minifarm.sensor.SensorService;
import kr.ac.hansung.cse.minifarm.control.ActuatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private final SensorService sensorService;
    private final ActuatorService actuatorService;
    private final ObjectMapper mapper = new ObjectMapper();

    // deviceId → session 저장
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("✅ WebSocket 연결됨: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode json = mapper.readTree(message.getPayload());
        String type = json.path("type").asText("");

        switch (type) {
            case "hello" -> { // 초기 등록
                String deviceId = json.path("deviceId").asText();
                sessions.put(deviceId, session);
                System.out.println("📡 Device 등록: " + deviceId);
            }

            case "sensor" -> {
                // 예: {"type":"sensor","deviceId":"pi-001","sensorType":"TEMP","value":24.3}
                sensorService.ingestFromWebSocket(json);
            }

            case "commandAck" -> {
                // 예: {"type":"commandAck","deviceId":"pi-001","commandId":"...","status":"EXECUTED"}
                actuatorService.updateCommandStatus(json);
            }

            default -> System.out.println("⚠️ Unknown WebSocket message: " + message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().removeIf(s -> s.equals(session));
        System.out.println(" WebSocket 연결 종료: " + session.getId());
    }

    // 서버 → Pi 명령 전송
    public void sendCommand(String deviceId, Map<String, Object> payload) {
        WebSocketSession session = sessions.get(deviceId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(mapper.writeValueAsString(payload)));
                System.out.println(" 명령 전송됨 → " + deviceId);
            } catch (Exception e) {
                System.err.println("WebSocket 전송 실패: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ WebSocket 세션 없음: " + deviceId);
        }
    }
}


/*import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper = new ObjectMapper();
    private final SensorService sensorService;
    private final ActuatorService actuatorService;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("Connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode json = mapper.readTree(message.getPayload());
        String type = json.get("type").asText();

        switch (type) {
            case "sensor":
                // {type:"sensor", deviceId:"...", sensorType:"TEMP", value:24.5}
                sensorService.ingestFromWebSocket(json);
                break;
            case "commandAck":
                // {type:"commandAck", commandId:"...", status:"EXECUTED"}
                actuatorService.updateCommandStatus(json);
                break;
            default:
                System.out.println("Unknown message: " + message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        System.out.println("Disconnected: " + session.getId());
    }

    // 서버 → Pi (액추에이터 제어 명령)
    public void sendCommand(String deviceId, Map<String,Object> payload) {
        sessions.values().forEach(s -> {
            try {
                s.sendMessage(new TextMessage(mapper.writeValueAsString(payload)));
            } catch (IOException e) { e.printStackTrace(); }
        });
    }
}
*/