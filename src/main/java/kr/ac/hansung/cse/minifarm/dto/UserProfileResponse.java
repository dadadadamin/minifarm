package kr.ac.hansung.cse.minifarm.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//클라이언트로 내려주는 유저 프로필 정보(비밀번호는 포함하지 않음)

@Getter
@Setter
@Builder
public class UserProfileResponse {

    private Long id;
    private String nickname;
    private String email;
    private String job;
    private Integer age;
    private String gender;
}
