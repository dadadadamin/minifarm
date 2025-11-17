package kr.ac.hansung.cse.minifarm.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlantInfoResponse {

    private Long id;

    //이름
    private String name;

    // 재배 난이도 (쉬움/보통/어려움)
    private String difficulty;

    // 적정 온도
    private Double tempMin;
    private Double tempMax;

    // 적정 습도
    private Double humidityMin;
    private Double humidityMax;

    // 적정 조도 (우선은 텍스트로)
    private String lightLevel;

    // LED 파장 (우선은 단순 문자열)
    private String ledInfo;

    // 양액(EC) 범위
    private Double ecMin;
    private Double ecMax;
}
