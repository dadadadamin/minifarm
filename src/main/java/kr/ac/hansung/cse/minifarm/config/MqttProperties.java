package kr.ac.hansung.cse.minifarm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

//application.properties 에서 MQTT 관련 설정을 읽어오는 클래스
@Getter
@Setter
@ConfigurationProperties(prefix = "minifarm.mqtt")
public class MqttProperties {

    private String username;
    private String password;

    //브로커 URL 예: tcp://localhost:1883
    private String brokerUrl;

    //서버에서 사용할 MQTT 클라이언트 ID
    private String clientId;

    //telemetry 구독 토픽 (와일드카드 사용 가능)
    //ex) minifarm/device/+/telemetry
    private String topicTelemetry;
}
