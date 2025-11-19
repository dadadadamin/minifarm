// src/main/java/kr/ac/hansung/cse/minifarm/controller/DeviceController.java
package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.device.DeviceCardResponse;
import kr.ac.hansung.cse.minifarm.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // 기기 선택 화면용: 내 장치 목록 조회
    // 예: GET /api/devices/my?userId=1
    @GetMapping("/my")
    public List<DeviceCardResponse> getMyDevices(@RequestParam Long userId) {
        return deviceService.getDevicesByUser(userId);
    }
}
