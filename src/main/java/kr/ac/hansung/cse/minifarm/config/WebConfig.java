package kr.ac.hansung.cse.minifarm.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry reg) {
        reg.addMapping("/**").allowedOrigins("http://localhost:3000","http://localhost:5173","http://localhost:8081")
                .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS").allowCredentials(true);
    }
}
