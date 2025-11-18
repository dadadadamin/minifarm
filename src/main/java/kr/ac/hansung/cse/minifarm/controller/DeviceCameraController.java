package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceCameraController {

    private final DiagnosisService diagnosisService;

    // 라즈베리파이가 촬영한 사진을 서버로 업로드
    @PostMapping(
            value = "/{deviceId}/camera/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void uploadFromDevice(
            @PathVariable("deviceId") Long deviceId,
            @RequestPart("requestId") String requestId,
            @RequestPart("image") MultipartFile image
    ) {
        diagnosisService.handleDeviceImageUpload(deviceId, requestId, image);
    }
}
