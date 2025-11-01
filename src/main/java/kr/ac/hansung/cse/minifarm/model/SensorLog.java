package kr.ac.hansung.cse.minifarm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="sensor_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SensorLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="device_id", nullable=false)
    private Device device;

    @Column(nullable=false) private String sensorType;
    @Column(nullable=false) private Double sensorValue;
    @Column(nullable=false) private OffsetDateTime recordedAt;
    private Integer slotNo;

    @Column(columnDefinition = "jsonb") private String meta; // 간단히 문자열로 시작
}
