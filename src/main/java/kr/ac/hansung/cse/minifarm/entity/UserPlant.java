package kr.ac.hansung.cse.minifarm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


//참조키 많으므로 수정시 주의!!!
// user_plants 테이블과 매핑되는 엔티티
//사용자가 실제로 기르기로 선택한 "내 식물(내 화분)" 정보
@Entity
@Table(name = "user_plants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPlant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 식물을 기르는 사용자
    //users.id FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //어떤 종류의 식물인지 (기본 정보 참조)
    //plants_info.id FK
    //선택값 (직접 입력만 허용하는 경우 null 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_info_id")
    private PlantInfo plantInfo;

    //사용자가 이 식물에 붙인 별칭-예: "베란다 토마토 1"
    @Column(length = 100)
    private String nickname;

    //기르기 시작한 날짜, 캘린더나 통계를 위해 사용
    //가급적 default 오늘로
    @Column(name = "started_at", nullable = false)
    private LocalDate startedAt;

    //이 식물이 놓인 위치 (텍스트) 예: "베란다 선반 2층"
    @Column(length = 255)
    private String location;

    // 기타 메모
    @Column(columnDefinition = "TEXT")
    private String memo;

    //레코드 생성 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
