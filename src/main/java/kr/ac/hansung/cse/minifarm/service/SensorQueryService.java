package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.Last24hResponse;
import kr.ac.hansung.cse.minifarm.entity.SensorLog;
import kr.ac.hansung.cse.minifarm.repository.SensorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 센서 데이터 조회용 서비스 (읽기 전용)
 */
@Service
@RequiredArgsConstructor
public class SensorQueryService {

    private final SensorLogRepository sensorLogRepository;

    /**
     * 특정 device 의 최근 24시간 센서 데이터 조회
     */
    public Last24hResponse getLast24Hours(Long deviceId) {
        LocalDateTime now = LocalDateTime.now();      // KST 기준
        LocalDateTime from = now.minusHours(24);

        List<SensorLog> logs = sensorLogRepository
                .findByDevice_IdAndCreatedAtBetweenOrderByCreatedAtAsc(deviceId, from, now);

        Last24hResponse.Series tempSeries = buildSeries(logs, "°C", SensorLog::getTemperature);
        Last24hResponse.Series humSeries  = buildSeries(logs, "%",  SensorLog::getHumidity);
        Last24hResponse.Series illSeries  = buildSeries(logs, "lux",SensorLog::getIlluminance);
        Last24hResponse.Series co2Series  = buildSeries(logs, "ppm",SensorLog::getCo2);
        Last24hResponse.Series ecSeries   = buildSeries(logs, "mS/cm",SensorLog::getEc);

        return Last24hResponse.builder()
                .deviceId(deviceId)
                .from(from)
                .to(now)
                .intervalMinutes(10) // 10분 간격 수집 기준
                .temperature(tempSeries)
                .humidity(humSeries)
                .illuminance(illSeries)
                .co2(co2Series)
                .ec(ecSeries)
                .build();
    }

    // -------- 내부 유틸 --------

    private Last24hResponse.Series buildSeries(
            List<SensorLog> logs,
            String unit,
            Function<SensorLog, Double> extractor
    ) {
        List<Last24hResponse.Point> points = new ArrayList<>();

        for (SensorLog log : logs) {
            Double value = extractor.apply(log);
            if (value == null) {
                // 값이 없는 포인트는 그래프에서 제외
                continue;
            }
            points.add(
                    Last24hResponse.Point.builder()
                            .timestamp(log.getCreatedAt())
                            .value(value)
                            .build()
            );
        }

        return Last24hResponse.Series.builder()
                .unit(unit)
                .points(points)
                .build();
    }
}
