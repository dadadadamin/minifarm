package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.Last24hResponse;
import kr.ac.hansung.cse.minifarm.entity.SensorLog;
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

    //특정 기기의 최근 24시간 센서 데이터.
    //ex: GET /api/devices/1/sensors/last24h
    @GetMapping("/{userPlantId}/sensors/last24h")
    public Last24hResponse getLast24Hours(@PathVariable Long userPlantId) {
        return sensorQueryService.getLast24Hours(userPlantId);
    }

    //특정 userPlant 의 최신 센서 데이터 조회
    @GetMapping("/{userPlantId}/sensors/latest")
    public SensorLog getLatest(@PathVariable Long userPlantId) {
        return sensorQueryService.getLatest(userPlantId);
    }
}
