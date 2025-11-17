package kr.ac.hansung.cse.minifarm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

//회원가입 요청 DTO
//닉네임, 이메일, 비밀번호는 필수
//직업, 나이, 성별은 선택 (추가 정보 화면에서 건너뛸 수 있음)
@Getter
@Setter
public class UserSignupRequest {

    @NotBlank
    private String nickname;    // 닉네임

    @Email
    @NotBlank
    private String email;       // 이메일

    @NotBlank
    private String password;    // 비밀번호

    @NotBlank
    private String passwordConfirm; // 비밀번호 확인 (서버에서 일치 여부 체크)

    // 아래 3개는 선택 입력
    private String job;         // 직업
    private Integer age;        // 나이
    private String gender;      // 성별
}
