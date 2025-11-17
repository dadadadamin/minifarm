package kr.ac.hansung.cse.minifarm.dto.diary;

import lombok.Builder;
import lombok.Getter;

import java.util.List;


//타임라인(사진) 항목 +응답 DTO
@Getter
@Builder
public class DiaryTimelineResponse {

    private Long userPlantId;
    private String plantNickname; // "타임캡슐" 등
    private String plantName;     // 상추 / 바질 등

    private List<DiaryTimelineItemDto> items;
}
