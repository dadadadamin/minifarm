package kr.ac.hansung.cse.minifarm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/* diary 테이블과 매핑되는 엔티티
- 특정 user_plant(내 식물)에 대한 날짜별 일기(다이어리)
- 사진 1장과 텍스트 메모를 함께 저장
 */
@Entity
@Table(
        name = "diary",
        uniqueConstraints = {
                // 같은 user_plant, 같은 날짜에는 다이어리가 1개만 존재
                @UniqueConstraint(
                        name = "uq_diary_user_plant_date",
                        columnNames = {"user_plant_id", "diary_date"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 user_plant(내 식물)에 대한 다이어리인지
    // user_plants.id FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_plant_id", nullable = false)
    private UserPlant userPlant;

    //다이어리를 작성한 날짜
    //캘린더 화면에서 날짜별로 보여줄 때 기준
    @Column(name = "diary_date", nullable = false)
    private LocalDate diaryDate;

    //그날의 메모 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    //사진이 저장된 경로(URL) 예: S3 URL, 서버의 파일 경로 등
    //s3탑재예정
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    //다이어리 생성 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 마지막 수정 시각 (수정이 없으면 null)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
