package kr.ac.hansung.cse.minifarm.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {

    @Value("${minifarm.mqtt.broker:tcp://localhost:1883}")
    private String broker;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setServerURIs(new String[]{broker});
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(opts);
        return factory;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MqttPahoMessageHandler mqttOutbound(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler h = new MqttPahoMessageHandler("minifarm-server", factory);
        h.setAsync(true);
        h.setDefaultQos(0);
        return h;
    }
}
