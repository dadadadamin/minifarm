package kr.ac.hansung.cse.minifarm.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 특정 device 의 최근 24시간 센서 데이터 응답 DTO.
 * 하단 그래프 탭(온도/습도/조도/EC/CO2)에 사용.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Last24hResponse {

    private Long userPlantId;
    private LocalDateTime from;
    private LocalDateTime to;
    private Integer intervalMinutes;   // 지금은 10분 고정이라고 가정

    private Series temperature;
    private Series humidity;
    private Series illuminance;
    private Series co2;
    private Series ec;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Series {
        private String unit;                 // "°C", "%", "lux", "ppm", "mS/cm"
        private List<Point> points;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Point {
        private LocalDateTime timestamp;
        private Double value;
    }
}
