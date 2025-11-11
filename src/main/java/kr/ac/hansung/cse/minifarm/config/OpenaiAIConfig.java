package kr.ac.hansung.cse.minifarm.config;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    private final Dotenv dotenv = Dotenv.load(); // 자동으로 .env 로드

    @Bean
    public OkHttpClient openAIHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    public String getApiKey() {
        return dotenv.get("OPENAI_API_KEY");
    }
}
