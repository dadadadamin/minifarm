package kr.ac.hansung.cse.minifarm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.hansung.cse.minifarm.config.OpenAIConfig;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient client;
    private final OpenAIConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public OpenAIService(OkHttpClient client, OpenAIConfig config) {
        this.client = client;
        this.config = config;
    }

    public String analyzePlantDisease(String prompt) {
        try {
            String json = """
            {
              "model": "gpt-4o-mini",
              "messages": [
                {"role": "system", "content": "너는 식물 병해를 진단하는 전문가야."},
                {"role": "user", "content": "%s"}
              ],
              "temperature": 0.2
            }
            """.formatted(prompt);

            RequestBody body = RequestBody.create(
                    json, MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(OPENAI_API_URL)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    throw new RuntimeException("OpenAI API 오류: " + response);

                JsonNode node = mapper.readTree(response.body().string());
                return node.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "OpenAI API 호출 중 오류 발생: " + e.getMessage();
        }
    }
}
