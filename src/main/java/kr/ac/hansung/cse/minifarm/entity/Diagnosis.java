package kr.ac.hansung.cse.minifarm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 내 식물에 대한 진단인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_plant_id", nullable = false)
    private UserPlant userPlant;

    // 진단에 사용된 사진 경로 (S3 URL 등)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 건강 상태 요약
    @Column(name = "health_summary", columnDefinition = "TEXT")
    private String healthSummary;

    // NORMAL / WARNING / DANGER 등
    @Column(name = "disease_status", length = 50)
    private String diseaseStatus;

    // 더 자세한 설명 또는 JSON 문자열
    @Column(name = "disease_details", columnDefinition = "TEXT")
    private String diseaseDetails;

    // 관리 팁/조언
    @Column(name = "advice", columnDefinition = "TEXT")
    private String advice;

    // 예상 수확 시기
    @Column(name = "harvest_prediction_date")
    private LocalDate harvestPredictionDate;

    // DEVICE / MOBILE
    @Column(name = "source_type", length = 20, nullable = false)
    private String sourceType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
