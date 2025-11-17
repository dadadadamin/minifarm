package kr.ac.hansung.cse.minifarm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

//비밀번호 변경 요청 DTO
//현재 비밀번호 + 새 비밀번호 + 새 비밀번호 확인
@Getter
@Setter
public class UserPasswordChangeRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String newPasswordConfirm;
}
