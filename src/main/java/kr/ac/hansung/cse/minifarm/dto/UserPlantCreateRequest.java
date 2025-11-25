package kr.ac.hansung.cse.minifarm.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserPlantCreateRequest {

    //private Long deviceId;  //삭제
    private Long plantId;        // 어떤 식물을 선택했는지
    private String nickname;     // 별칭 (선택)
    private LocalDate startedAt; // 재배 시작일 (선택, 없으면 오늘로 처리)
}
