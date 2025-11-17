package kr.ac.hansung.cse.minifarm.dto.diary;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

//Calendar 전체 응답 dto
@Getter
@Builder
public class DiaryCalendarResponse {

    private Long userPlantId;
    private String plantNickname;   // 예: "타임캡슐"
    private String plantName;       // 예: "상추" (PlantInfo.name)

    private LocalDate firstPlantedDate; // 첫 재배일(userPlant.plantedAt)
    private long daysSincePlanted;      // 재배일수
    private long photoCount;            // 사진이 있는 다이어리 수

    private int year;   // 요청한 연도
    private int month;  // 요청한 월

    private List<DiaryCalendarDayDto> days; // month 내 날짜 리스트
}
