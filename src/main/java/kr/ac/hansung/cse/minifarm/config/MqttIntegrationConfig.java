package kr.ac.hansung.cse.minifarm.config;

import kr.ac.hansung.cse.minifarm.mqtt.TelemetryMessageHandler;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

/**
 * 라즈베리파이 -> MQTT 브로커 -> Spring 으로 들어오는 입구 설정.
 * DSL(IntegrationFlows) 없이 채널 + 인바운드 어댑터만 사용.
 */
@Configuration
@EnableIntegration
@IntegrationComponentScan
@RequiredArgsConstructor
public class MqttIntegrationConfig {

    private final MqttProperties mqttProperties;
    // TelemetryMessageHandler 는 채널에 @ServiceActivator 로 붙일 예정

    /**
     * MQTT 클라이언트 팩토리
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{mqttProperties.getBrokerUrl()});
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());

        factory.setConnectionOptions(options);
        return factory;
    }

    /**
     * MQTT 메시지가 처음 들어오는 채널 이름: mqttInputChannel
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT 인바운드 어댑터
     * - minifarm.mqtt.topic-telemetry 에 설정한 토픽을 구독
     * - 수신한 메시지를 mqttInputChannel 로 보냄
     */
    @Bean
    public MessageProducer mqttInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        mqttProperties.getClientId(),
                        mqttClientFactory(),
                        mqttProperties.getTopicTelemetry()
                );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);

        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }
}
