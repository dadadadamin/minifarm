package kr.ac.hansung.cse.minifarm.dto;

import lombok.Getter;
import lombok.Setter;

//프로필 수정 요청 DTO
//일부 필드만 수정할 수 있도록 모두 선택값으로 둠

@Getter
@Setter
public class UserProfileUpdateRequest {

    private String nickname;
    private String job;
    private Integer age;
    private String gender;
}
