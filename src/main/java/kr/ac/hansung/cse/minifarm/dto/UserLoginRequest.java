package kr.ac.hansung.cse.minifarm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

//로그인 요청 DTO
//로그인 화면의 이메일/비밀번호와 매핑
@Getter
@Setter
public class UserLoginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
