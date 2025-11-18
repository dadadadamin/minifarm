package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.diagnosis.*;
import kr.ac.hansung.cse.minifarm.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    // 1) 라즈베리파이 촬영 요청
    @PostMapping("/device/request")
    public DeviceDiagnosisRequestResponse requestDeviceDiagnosis(
            @RequestBody DeviceDiagnosisRequestDto requestDto
    ) {
        return diagnosisService.requestDeviceDiagnosis(requestDto);
    }

    // 2) 라즈베리파이 촬영 결과(진단 결과) 폴링
    @GetMapping("/device/result")
    public DeviceDiagnosisResultResponse getDeviceDiagnosisResult(
            @RequestParam("requestId") String requestId
    ) {
        return diagnosisService.getDeviceDiagnosisResult(requestId);
    }

    // 3) 모바일 카메라로 바로 업로드하여 진단
    @PostMapping(
            value = "/mobile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DiagnosisResponse analyzeFromMobile(
            @RequestPart("userPlantId") Long userPlantId,
            @RequestPart(value = "symptomNote", required = false) String symptomNote,
            @RequestPart("image") MultipartFile image
    ) {
        return diagnosisService.analyzeFromMobile(userPlantId, image, symptomNote);
    }
}
