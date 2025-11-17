package kr.ac.hansung.cse.minifarm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// plants_info 테이블과 매핑되는 엔티티
//특정 작물(식물)의 "기본 재배 정보"를 저장 - 재배 난이도, 적정 온/습도, CO2, EC, 양액 주기 등)
//여러 사용자가 공통으로 참고하는 기준 정보
@Entity
@Table(name = "plants_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //식물 이름 (예: 방울토마토, 상추)
    @Column(nullable = false, length = 100)
    private String name;

    //학명 (선택) - 나중에 삭제할수도 있음
    @Column(name = "scientific_name", length = 150)
    private String scientificName;

    //재배 난이도 (예: EASY, MEDIUM, HARD)
    @Column(length = 20)
    private String difficulty;

    //생장 환경 관련
    //
    //권장 온도 범위 (최소/최대)
    @Column(name = "temp_min")
    private Double tempMin;

    @Column(name = "temp_max")
    private Double tempMax;

    // 권장 습도 범위 (최소/최대)
    @Column(name = "humidity_min")
    private Double humidityMin;

    @Column(name = "humidity_max")
    private Double humidityMax;

    //빛(조도) 필요 수준 예: LOW / MEDIUM / HIGH
    //추후 조광량에 따라 "실내 밝은 창가" 같이 설명 형태로 변경 권장
    @Column(name = "light_level", length = 50)
    private String lightLevel;

    //양액(영양분) 또는 물 주기 (일 단위) 예: 3 → 3일마다 급수
    //가급적 물 주기로 맞추기
    @Column(name = "nutrient_cycle_days")
    private Short nutrientCycleDays;

    // CO2 권장 범위 (최소/최대)
    @Column(name = "co2_min")
    private Double co2Min;

    @Column(name = "co2_max")
    private Double co2Max;

    //EC(토양 영양분) 권장 범위 (최소/최대)
    @Column(name = "ec_min")
    private Double ecMin;

    @Column(name = "ec_max")
    private Double ecMax;

    // 정보 생성 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
