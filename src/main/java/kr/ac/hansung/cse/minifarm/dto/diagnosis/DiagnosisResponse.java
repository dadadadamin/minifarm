package kr.ac.hansung.cse.minifarm.dto.diagnosis;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

//진단 결과 공통 응답 dto
@Getter
@Builder
public class DiagnosisResponse {

    private Long id;
    private Long userPlantId;
    private String imageUrl;

    private String healthSummary;
    private String diseaseStatus;
    private String diseaseDetails;
    private String advice;

    private LocalDate harvestPredictionDate;

    private String sourceType;
    private LocalDateTime createdAt;
}
