package kr.ac.hansung.cse.minifarm.dto.diary;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;


//타임라인(사진) 항목 +응답 DTO
@Getter
@Builder
public class DiaryTimelineItemDto {

    private Long id;
    private LocalDate diaryDate;
    private String imageUrl;
    private String content;
}
