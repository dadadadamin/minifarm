package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.diagnosis.DeviceDiagnosisRequestDto;
import kr.ac.hansung.cse.minifarm.dto.diagnosis.DeviceDiagnosisRequestResponse;
import kr.ac.hansung.cse.minifarm.dto.diagnosis.DeviceDiagnosisResultResponse;
import kr.ac.hansung.cse.minifarm.dto.diagnosis.DiagnosisResponse;
import kr.ac.hansung.cse.minifarm.entity.*;
import kr.ac.hansung.cse.minifarm.entity.enums.CaptureRequestStatus;
import kr.ac.hansung.cse.minifarm.entity.enums.DiagnosisSource;
import kr.ac.hansung.cse.minifarm.repository.DeviceCaptureRequestRepository;
import kr.ac.hansung.cse.minifarm.repository.DiagnosisRepository;
import kr.ac.hansung.cse.minifarm.repository.UserPlantRepository;
import kr.ac.hansung.cse.minifarm.service.ai.AiVisionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

//라즈베리파이 달려있는 카메라통신-통신불가시 핸드폰카메라로 촬영하여 진단 플로우로 가려했으나
//단계가 너무 복잡해 그냥 카메라로 사진까지 같이 넘겨받으면 진단으로
@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosisService {

    // 타임아웃 기준 (초)
    //private static final int DEVICE_TIMEOUT_SEC = 10;

    private final UserPlantRepository userPlantRepository;
    //private final DeviceCaptureRequestRepository deviceCaptureRequestRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final AiVisionClient aiVisionClient;

    // TODO: 이미지 저장 로직 (S3 / 로컬)
    // 지금은 단순히 파일 이름만 돌려주는 형태로 둡니다.
    private String saveImageAndGetUrl(MultipartFile image, String prefix) {
        // TODO: 실제 저장 로직 구현
        // 예: S3에 prefix + UUID + 확장자 형식으로 업로드 후 URL 반환
        String fakeUrl = "https://example.com/images/" + prefix + "-" + UUID.randomUUID() + ".jpg";
        return fakeUrl;
    }

    // 4. 모바일 카메라로 바로 업로드하여 진단
    public DiagnosisResponse analyzeFromMobile(
            Long userPlantId,
            MultipartFile imageFile,
            String symptomNote
    ) {
        UserPlant userPlant = userPlantRepository.findById(userPlantId)
                .orElseThrow(() -> new IllegalArgumentException("userPlant not found: " + userPlantId));

        String imageUrl = saveImageAndGetUrl(imageFile, "mobile");

        Diagnosis diagnosis = createAndSaveDiagnosis(
                userPlant,
                imageFile,
                imageUrl,
                symptomNote,
                DiagnosisSource.MOBILE
        );

        return mapToDiagnosisResponse(diagnosis);
    }

    // 실제 Diagnosis 엔티티 생성 + 저장 공통 부분
    private Diagnosis createAndSaveDiagnosis(
            UserPlant userPlant,
            MultipartFile imageFile,
            String imageUrl,
            String symptomNote,
            DiagnosisSource source
    ) {
        // 1) AI 분석 (Vision 호출)
        DiagnosisResponse aiResult = aiVisionClient.analyze(
                userPlant,
                imageFile,
                imageUrl,
                symptomNote,
                source
        );

        // 2) 엔티티로 저장
        Diagnosis diagnosis = Diagnosis.builder()
                .userPlant(userPlant)
                .imageUrl(imageUrl)
                .healthSummary(aiResult.getHealthSummary())
                .diseaseStatus(aiResult.getDiseaseStatus())
                .diseaseDetails(aiResult.getDiseaseDetails())
                .advice(aiResult.getAdvice())
                .harvestPredictionDate(aiResult.getHarvestPredictionDate())
                .sourceType(source.name())
                .build();

        diagnosisRepository.save(diagnosis);
        return diagnosis;
    }

    private DiagnosisResponse mapToDiagnosisResponse(Diagnosis d) {
        return DiagnosisResponse.builder()
                .id(d.getId())
                .userPlantId(d.getUserPlant().getId())
                .imageUrl(d.getImageUrl())
                .healthSummary(d.getHealthSummary())
                .diseaseStatus(d.getDiseaseStatus())
                .diseaseDetails(d.getDiseaseDetails())
                .advice(d.getAdvice())
                .harvestPredictionDate(d.getHarvestPredictionDate())
                .sourceType(d.getSourceType())
                .createdAt(d.getCreatedAt())
                .build();
    }
/*
    // 1. 모바일 → 라즈베리파이 촬영 요청
    public DeviceDiagnosisRequestResponse requestDeviceDiagnosis(DeviceDiagnosisRequestDto dto) {
        UserPlant userPlant = userPlantRepository.findById(dto.getUserPlantId())
                .orElseThrow(() -> new IllegalArgumentException("userPlant not found: " + dto.getUserPlantId()));

        Device device = userPlant.getDevice();
        if (device == null) {
            throw new IllegalStateException("이 식물에 연결된 장치가 없습니다.");
        }

        String requestId = UUID.randomUUID().toString();

        DeviceCaptureRequest request = DeviceCaptureRequest.builder()
                .requestId(requestId)
                .userPlant(userPlant)
                .device(device)
                .status(CaptureRequestStatus.WAITING)
                .build();

        deviceCaptureRequestRepository.save(request);

        // 여기서 MQTT 메시지 발행 (디바이스에게 촬영 요청)
        // TODO: MqttPublisher.publishCaptureCommand(device, requestId);

        return DeviceDiagnosisRequestResponse.builder()
                .requestId(requestId)
                .timeoutSec(DEVICE_TIMEOUT_SEC)
                .build();
    }

    // 2. 라즈베리파이 → 서버: 촬영한 이미지 업로드
    public void handleDeviceImageUpload(
            Long deviceId,
            String requestId,
            MultipartFile imageFile
    ) {
        DeviceCaptureRequest request = deviceCaptureRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("capture request not found: " + requestId));

        if (!request.getDevice().getId().equals(deviceId)) {
            throw new IllegalStateException("요청한 deviceId와 실제 요청의 deviceId가 다릅니다.");
        }

        if (request.getStatus() == CaptureRequestStatus.TIMEOUT ||
                request.getStatus() == CaptureRequestStatus.COMPLETED) {
            // 이미 타임아웃/완료된 요청이면 무시하거나 예외 처리
            return;
        }

        request.setStatus(CaptureRequestStatus.RECEIVED);

        // 1) 이미지 저장
        String imageUrl = saveImageAndGetUrl(imageFile, "device");
        request.setImageUrl(imageUrl);

        // 2) OpenAI Vision 분석
        request.setStatus(CaptureRequestStatus.PROCESSING);

        Diagnosis diagnosis = createAndSaveDiagnosis(
                request.getUserPlant(),
                imageFile,
                imageUrl,
                null,                 // 라즈베리파이 경로에선 symptomNote 아직 없음
                DiagnosisSource.DEVICE
        );

        // 3) 요청 상태 업데이트
        request.setDiagnosis(diagnosis);
        request.setStatus(CaptureRequestStatus.COMPLETED);
        // @Transactional 이므로 메서드 종료 시 자동 flush
    }

    // 3. 모바일 폴링: 라즈베리파이 진단 결과 조회
    @Transactional(readOnly = true)
    public DeviceDiagnosisResultResponse getDeviceDiagnosisResult(String requestId) {
        DeviceCaptureRequest request = deviceCaptureRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("capture request not found: " + requestId));

        CaptureRequestStatus status = request.getStatus();
        LocalDateTime now = LocalDateTime.now();
        long elapsedSec = Duration.between(request.getCreatedAt(), now).getSeconds();

        // 타임아웃 처리
        if ((status == CaptureRequestStatus.WAITING || status == CaptureRequestStatus.PROCESSING)
                && elapsedSec > DEVICE_TIMEOUT_SEC) {

            // 읽기 전용 트랜잭션이라 상태 변경용 별도 메서드를 두는 게 깔끔하지만,
            // 여기서는 간단히 예외로 TIMEOUT 응답만 돌려줍니다.
            return DeviceDiagnosisResultResponse.builder()
                    .status("TIMEOUT")
                    .message("디바이스에서 응답이 없습니다. 휴대폰으로 촬영해 주세요.")
                    .build();
        }

        if (status == CaptureRequestStatus.WAITING || status == CaptureRequestStatus.RECEIVED
                || status == CaptureRequestStatus.PROCESSING) {

            return DeviceDiagnosisResultResponse.builder()
                    .status(status.name())
                    .build();
        }

        if (status == CaptureRequestStatus.FAILED || status == CaptureRequestStatus.TIMEOUT) {
            return DeviceDiagnosisResultResponse.builder()
                    .status(status.name())
                    .message(request.getErrorMessage())
                    .build();
        }

        // COMPLETED
        Diagnosis diagnosis = request.getDiagnosis();
        if (diagnosis == null) {
            return DeviceDiagnosisResultResponse.builder()
                    .status("FAILED")
                    .message("진단 결과가 존재하지 않습니다.")
                    .build();
        }

        DiagnosisResponse dto = mapToDiagnosisResponse(diagnosis);
        return DeviceDiagnosisResultResponse.builder()
                .status("COMPLETED")
                .diagnosis(dto)
                .build();
    }
*/


}
