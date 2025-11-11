package kr.ac.hansung.cse.minifarm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.hansung.cse.minifarm.controller.DeviceWebSocketHandler; // 핸들러가 controller 패키지에 있을 때
import lombok.RequiredArgsConstructor;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActuatorService {

    // MQTT를 아직 안 쓰면 빈 주입을 선택(Optional)으로 바꾸거나 MqttConfig 추가하세요.
    private final MqttPahoMessageHandler mqttOutbound;
    private final DeviceWebSocketHandler wsHandler;
    private final ObjectMapper mapper = new ObjectMapper();

    public void sendCommand(UUID deviceId, String actuatorType, Map<String, Object> payload) {
        try {
            // 1) MQTT publish (동시에 지원)
            String topic = "minifarm/" + deviceId + "/actuator/" + actuatorType.toLowerCase();
            String json = mapper.writeValueAsString(payload);
            mqttOutbound.handleMessage(
                    MessageBuilder.withPayload(json).setHeader("mqtt_topic", topic).build()
            );

            // 2) WebSocket push
            wsHandler.sendCommand(deviceId.toString(), Map.of(
                    "type", "actuator",
                    "device", actuatorType.toLowerCase(),
                    "payload", payload,
                    "commandId", UUID.randomUUID().toString(),
                    "createdAt", OffsetDateTime.now().toString()
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send command", e);
        }
    }
}
