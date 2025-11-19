package kr.ac.hansung.cse.minifarm.service.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import io.github.cdimascio.dotenv.Dotenv;
import kr.ac.hansung.cse.minifarm.dto.diagnosis.DiagnosisResponse;
import kr.ac.hansung.cse.minifarm.entity.UserPlant;
import kr.ac.hansung.cse.minifarm.entity.enums.DiagnosisSource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

// OpenAI Vision API 호출을 감싸는 역할
@Component
public class AiVisionClient {

    private final OpenAIClient openai;

    public AiVisionClient() {
        // .env 에서 OPENAI_API_KEY 읽기
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()   // 혹시 .env 없으면 NPE 방지
                .load();

        String apiKey = dotenv.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 가 설정되어 있지 않습니다 (.env 확인).");
        }

        // OkHttp 기반 클라이언트 생성
        this.openai = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * 이미지 바이트 + 식물 이름을 기반으로 OpenAI에 질의.
     * 지금은 Vision 전용 타입 대신, base64 문자열을 텍스트 프롬프트에 포함시키는 방식으로 구현.
     */
    public String analyzePlant(byte[] imageBytes, String plantName) {

        // Base64 인코딩
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String systemPrompt = """
                너는 작물 생육 및 병해충 진단을 도와주는 농업 전문가 AI다.
                가능한 한 구체적이고 실용적인 한국어 조언을 제공해라.
                """;

        String userPrompt = """
                이 식물은 '%s'이다.
                아래는 사용자가 촬영한 사진의 base64 인코딩 데이터이다.
                이 데이터를 참고해서 다음 내용을 한국어로 정리해줘.

                1) 전반적인 건강 상태 요약
                2) 병해충 의심 여부 (있다면 어떤 종류인지)
                3) 지금 당장 해줄 수 있는 관리/조치 방법
                4) 앞으로 1~2주 동안의 관리 팁
                5) 수확 가능한 작물이라면, 대략적인 수확 시기 예상

                base64 이미지 데이터 (참고용):
                %s
                """.formatted(plantName, base64Image);

        // ChatCompletion 요청 파라미터 생성
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model("gpt-4.1-mini")
                .addSystemMessage(systemPrompt)
                .addUserMessage(userPrompt)
                .maxTokens(800)
                .build();

        // OpenAI 호출
        ChatCompletion completion = openai.chat()
                .completions()
                .create(params);

        if (completion.choices().isEmpty()) {
            return "OpenAI 응답이 비어 있습니다.";
        }

        // message().toString() 으로 전체 내용을 문자열로 반환
        // (SDK의 content 파싱은 버전별로 타입이 달라질 수 있어서, 우선 전체를 문자열로 받도록 구성)
        return completion.choices().get(0).message().toString();
    }


    //테스트용
    public DiagnosisResponse analyze(
            UserPlant userPlant,
            MultipartFile image,
            String imageUrl,
            String symptomNote,
            DiagnosisSource source
    ) {
        try {
            // (1) 이미지 바이트
            byte[] bytes = image.getBytes();

            // (2) 식물 이름
            String plantName = (userPlant.getPlantInfo() != null)
                    ? userPlant.getPlantInfo().getName()
                    : "알 수 없는 식물";

            // (3) Vision API 호출 -> AI 분석 결과 (한 덩어리 텍스트)
            String aiText = analyzePlant(bytes, plantName);

            // (4) 일단 텍스트 그대로 저장
            // 추후 파싱해서 구체적으로 나눌 수도 있음
            return DiagnosisResponse.builder()
                    .id(null)
                    .userPlantId(userPlant.getId())
                    .imageUrl(imageUrl)
                    .healthSummary(aiText)
                    .diseaseStatus("UNKNOWN")   // TODO: aiText 분석해서 상태 추론
                    .diseaseDetails(aiText)
                    .advice("AI 분석 결과를 참고하세요.")
                    .harvestPredictionDate(LocalDate.now().plusDays(7))
                    .sourceType(source.name())
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("이미지 읽기 실패", e);
        }
    }



    /*
    // 나중에 실제 DiagnosisResponse 까지 한 번에 구성하고 싶다면 아래 템플릿 참고해서 사용하면 됨.
    public DiagnosisResponse analyze(
            UserPlant userPlant,
            MultipartFile image,
            String imageUrl,
            String symptomNote,
            DiagnosisSource source
    ) throws IOException {

        byte[] bytes = image.getBytes();
        String plantName = (userPlant.getPlantInfo() != null)
                ? userPlant.getPlantInfo().getName()
                : "알 수 없는 식물";

        String aiText = analyzePlant(bytes, plantName);

        // 아주 단순히 AI 응답 전체를 healthSummary 에 넣는 예시
        // (실제로는 파싱해서 healthSummary / diseaseStatus / advice 등을 나눠도 됨)
        return DiagnosisResponse.builder()
                .id(null)
                .userPlantId(userPlant.getId())
                .imageUrl(imageUrl)
                .healthSummary(aiText)
                .diseaseStatus("UNKNOWN") // TODO: aiText 분석해서 NORMAL / WARNING / DANGER 등으로 매핑
                .diseaseDetails(aiText)
                .advice("AI 응답을 참고하여 수동으로 해석한 뒤, 후에 로직을 개선하세요.")
                .harvestPredictionDate(LocalDate.now().plusDays(10))
                .sourceType(source.name())
                .createdAt(LocalDateTime.now())
                .build();
    }
    */
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
