package kr.ac.hansung.cse.minifarm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


//users 테이블과 매핑되는 엔티티/ 회원 기본 정보 (로그인, 프로필 등)
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    //기본 키 (PK) BIGSERIAL → Long + IDENTITY 전략
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //로그인에 사용하는 이메일 NOT NULL UNIQUE
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    //비밀번호(해시된 값 저장) , 실제 서비스에서는 평문 저장금지
    @Column(nullable = false, length = 255)
    private String password;

    //nickname(앱에서 보이는 이름)
    @Column(nullable = false, length = 100)
    private String nickname;

    //직업 정보 (null 값 허용)
    @Column(length = 100)
    private String job;

    //나이 (null 값 허용)
    private Integer age;

    //성별 (null 값 허용)
    @Column(length = 20)
    private String gender;

    //계정 생성 시각 DB default: CURRENT_TIMESTAMP
     //JPA에서 저장할 때 직접 세팅해줘도 됨
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
