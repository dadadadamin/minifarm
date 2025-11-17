package kr.ac.hansung.cse.minifarm.dto.diary;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;


//단일 다이어리 응답용
@Getter
@Builder
public class DiaryResponse {

    private Long id;
    private Long userPlantId;
    private LocalDate diaryDate;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
