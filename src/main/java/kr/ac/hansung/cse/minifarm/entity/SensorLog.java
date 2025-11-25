package kr.ac.hansung.cse.minifarm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/* sensor_logs 테이블과 매핑되는 엔티티
 * - 특정 장치(Device)의 시점별 센서 값을 기록
 * - 온도, 습도, 조도, CO2, EC 를 한 번에 저장
 */
@Entity
@Table(name = "sensor_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //어느 장치에서 수집된 센서 데이터인지
    //devices.id FK
    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "device_id", nullable = false)
    //private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_plant_id", nullable = false)
    private UserPlant userPlant;


    //온도 (°C) 예: 23.5
    @Column(name = "temperature")
    private Double temperature;

    //습도 (%) 예: 45.3
    @Column(name = "humidity")
    private Double humidity;

    // 조도 (Lux 등)
    @Column(name = "illuminance")
    private Double illuminance;

    //CO2 농도 (ppm)
    @Column(name = "co2")
    private Double co2;

    // EC 값
    @Column(name = "ec")
    private Double ec;

    //센서 값이 기록된 시간
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
