package kr.ac.hansung.cse.minifarm;

import kr.ac.hansung.cse.minifarm.config.MqttProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(MqttProperties.class)
@SpringBootApplication
public class MinifarmApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinifarmApplication.class, args);
    }

}
