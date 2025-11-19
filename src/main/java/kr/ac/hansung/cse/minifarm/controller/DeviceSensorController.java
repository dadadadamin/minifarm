package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.Last24hResponse;
import kr.ac.hansung.cse.minifarm.service.SensorQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 기기별 센서 데이터 조회 API
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceSensorController {

    private final SensorQueryService sensorQueryService;

    /**
     * 특정 기기의 최근 24시간 센서 데이터.
     * 예: GET /api/devices/1/sensors/last24h
     */
    @GetMapping("/{deviceId}/sensors/last24h")
    public Last24hResponse getLast24Hours(@PathVariable Long deviceId) {
        return sensorQueryService.getLast24Hours(deviceId);
    }
}
