package kr.ac.hansung.cse.minifarm.mqtt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.hansung.cse.minifarm.service.SensorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * mqttInputChannel 로 들어온 MQTT 메시지를 처리하는 핸들러.
 * - JSON 파싱
 * - SensorLogService 에 위임해서 DB 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryMessageHandler {

    private final ObjectMapper objectMapper;
    private final SensorLogService sensorLogService;

    /**
     * MqttIntegrationConfig 에서 만든 mqttInputChannel 을 소비하는 메서드.
     */
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = message.getPayload().toString();

        log.info("[MQTT] topic={}, payload={}", topic, payload);

        try {
            // 1) JSON -> TelemetryMessage 로 변환
            TelemetryMessage telemetry =
                    objectMapper.readValue(payload, TelemetryMessage.class);

            if (telemetry.getDeviceId() == null) {
                log.warn("[MQTT] deviceId 가 없는 메시지입니다. payload={}", payload);
                return;
            }

            // 2) 서비스에 위임해서 sensor_logs 저장
            sensorLogService.saveTelemetry(telemetry);

        } catch (Exception e) {
            log.error("[MQTT] telemetry 처리 중 오류. payload={}", payload, e);
        }
    }

    // ================= MQTT JSON 매핑용 내부 클래스 =================

    /**
     * 라즈베리파이에서 보내는 JSON 형태
     *
     * {
     *   "deviceId": 3,
     *   "measuredAt": "2025-03-26T15:40:00+09:00",
     *   "metrics": {
     *     "temperature": 22.0,
     *     "humidity": 59.0,
     *     "illuminance": 820.0,
     *     "co2": null,
     *     "ec": null
     *   }
     * }
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelemetryMessage {

        private Long deviceId;
        private String measuredAt;   // 지금은 안 쓰고, 필요하면 나중에 LocalDateTime 으로 파싱
        private Metrics metrics;

        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public String getMeasuredAt() {
            return measuredAt;
        }

        public void setMeasuredAt(String measuredAt) {
            this.measuredAt = measuredAt;
        }

        public Metrics getMetrics() {
            return metrics;
        }

        public void setMetrics(Metrics metrics) {
            this.metrics = metrics;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metrics {

        private Double temperature;
        private Double humidity;
        private Double illuminance;
        private Double co2;
        private Double ec;

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getHumidity() {
            return humidity;
        }

        public void setHumidity(Double humidity) {
            this.humidity = humidity;
        }

        public Double getIlluminance() {
            return illuminance;
        }

        public void setIlluminance(Double illuminance) {
            this.illuminance = illuminance;
        }

        public Double getCo2() {
            return co2;
        }

        public void setCo2(Double co2) {
            this.co2 = co2;
        }

        public Double getEc() {
            return ec;
        }

        public void setEc(Double ec) {
            this.ec = ec;
        }
    }
}
