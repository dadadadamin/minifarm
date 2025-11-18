package kr.ac.hansung.cse.minifarm.service.ai;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionMessage;
import io.github.cdimascio.dotenv.Dotenv;
import kr.ac.hansung.cse.minifarm.dto.diagnosis.DiagnosisResponse;
import kr.ac.hansung.cse.minifarm.entity.UserPlant;
import kr.ac.hansung.cse.minifarm.entity.enums.DiagnosisSource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;


import com.openai.OpenAI;
import com.openai.models.ChatCompletionMessage;
import com.openai.models.ChatCompletionRequest;
import com.openai.models.ChatCompletionResponse;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
// OpenAI Vision API 호출을 감싸는 역할
@Component
public class AiVisionClient {
    private final OpenAIClient openai;

    public AiVisionClient() {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("OPENAI_API_KEY");

        this.openai = OpenAI.builder()
                .apiKey(apiKey)
                .build();
    }

    // Vision 분석 실행
    public String analyzePlant(byte[] imageBytes, String plantName) {

        // Base64 인코딩
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // 메시지 구성
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4.1-mini")   // Vision 가능 모델
                .messages(List.of(
                        ChatCompletionMessage.builder()
                                .role("system")
                                .content("너는 작물 생육 전문가이자 병해충 전문가 AI다.")
                                .build(),

                        ChatCompletionMessage.builder()
                                .role("user")
                                .content(
                                        "이 식물은 '" + plantName + "'이다. " +
                                                "사진을 보고 병해충 여부, 문제점, 해결 방법, 현재 건강 상태, 예상 수확 가능성 등을 분석해줘."
                                )
                                .build(),

                        // 이미지 첨부
                        ChatCompletionMessage.builder()
                                .role("user")
                                .content(
                                        List.of(
                                                ChatCompletionMessage.ContentPart.builder()
                                                        .type("input_image")
                                                        .imageUrl("data:image/jpeg;base64," + base64Image)
                                                        .build()
                                        )
                                )
                                .build()
                ))
                .build();

        ChatCompletionResponse response = openai.chat().create(request);

        // 결과 텍스트 반환
        return response.getChoices().get(0).getMessage().getContent();
    }


    /*

    // TODO: OpenAI SDK 또는 HTTP 클라이언트 주입


    public DiagnosisResponse analyze(
            UserPlant userPlant,
            MultipartFile image,
            String imageUrl,
            String symptomNote,
            DiagnosisSource source
    ) {
        // TODO: 여기에서 OpenAI Vision API 호출 구현
        // - image.getBytes() 또는 imageUrl 사용
        // - plantInfo, userPlant 정보 + symptomNote를 함께 프롬프트로 구성

        // 지금은 더미 응답
        return DiagnosisResponse.builder()
                .id(null) // 저장 전이므로 null
                .userPlantId(userPlant.getId())
                .imageUrl(imageUrl)
                .healthSummary("식물의 전반적인 건강 상태는 양호합니다.")
                .diseaseStatus("NORMAL")
                .diseaseDetails("병해충이 뚜렷하게 보이지 않습니다. 잎 색과 형태가 전반적으로 좋습니다.")
                .advice("현재 관리 방법을 유지하되, 잎 뒷면에 작은 점이나 벌레가 나타나는지 주기적으로 확인해 주세요.")
                .harvestPredictionDate(LocalDate.now().plusDays(10)) // 예시
                .sourceType(source.name())
                .createdAt(LocalDateTime.now())
                .build();
    }
     */
}
