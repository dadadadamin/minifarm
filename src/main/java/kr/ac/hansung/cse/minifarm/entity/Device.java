package kr.ac.hansung.cse.minifarm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//devices 테이블과 매핑되는 엔티티
//라즈베리파이 / 베란다 농부 수다키트 같은 "하드웨어 장치" 정보를 저장
//한 장치는 보통 한 사용자가 소유
// 현재는 라즈베리파이 장치 연결시 기록으로 대체
@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 장치를 등록한 사용자 (owner 개념)
     * - 꼭 필수는 아니므로 nullable 허용
     * - 나중에 로그인한 사용자 기준으로 "내 장치 목록" 조회 가능
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 장치 이름 (앱에서 보이는 이름)
     * 예: "베란다 키트 1", "방2 키트". "처음 기르는 상추"
     */
    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    /**
     * 장치 고유 시리얼 번호
     * - 라즈베리파이에서 서버로 보낼 때 이 값을 사용해서 자기 자신을 식별
     * - UNIQUE 제약 조건
     */
    @Column(name = "serial_no", nullable = false, unique = true, length = 100)
    private String serialNo;

    //설치 위치 정보 예: "거실 창가", "베란다 왼쪽"
    //null 허용
    @Column(length = 255)
    private String location;

    // 장치 등록 시각

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
