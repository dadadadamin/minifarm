package kr.ac.hansung.cse.minifarm.dto.diary;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;


// Calendar용 Day DTO
@Getter
@Builder
public class DiaryCalendarDayDto {

    private LocalDate date;      // 날짜
    private boolean hasDiary;    // 다이어리 존재 여부
    private Long diaryId;        // 있으면 id
    private String thumbnailUrl; // 간단하게 imageUrl 그대로 사용 (없으면 null)
}
