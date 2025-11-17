package kr.ac.hansung.cse.minifarm.controller;

import jakarta.validation.Valid;
import kr.ac.hansung.cse.minifarm.dto.*;
import kr.ac.hansung.cse.minifarm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//유저 관련 REST API 컨트롤러
// 로그인/회원가입/로그아웃/프로필 조회·수정/비밀번호 변경/회원탈퇴
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원가입
    //POST /api/users/signup
    @PostMapping("/signup")
    public ResponseEntity<UserProfileResponse> signup(
            @Valid @RequestBody UserSignupRequest request
    ) {
        UserProfileResponse response = userService.signup(request);
        return ResponseEntity.ok(response);
    }

    //로그인 POST /api/users/login
    //현재는 세션/토큰 없이 이메일+비밀번호 검증 후 UserProfileResponse만 반환
    //추후 JWT 토큰을 함께 내려주는 방식으로 확장
    @PostMapping("/login")
    public ResponseEntity<UserProfileResponse> login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        UserProfileResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    //로그아웃 POST /api/users/logout
    //지금 구현에서는 서버가 세션을 들고 있지 않기 때문에 클라이언트에서 토큰/로그인 상태를 지우는 용도로만 사용
    //나중에 서버 측 세션/리프레시 토큰을 쓰게 되면 여기서 무효화 로직 추가 예정
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // 별도 처리 없음 (stateless)
        return ResponseEntity.ok().build();
    }

    //프로필 조회 GET /api/users/{id}
    //실제 서비스에서는 id 대신토큰에서 유저 정보를 꺼내 사용하는 방식으로 바꾸는 방식으로 적용예정
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long id) {
        UserProfileResponse response = userService.getProfile(id);
        return ResponseEntity.ok(response);
    }

    // 프로필 수정 (이름/닉네임/직업/나이/성별) PUT /api/users/{id}/profile
    @PutMapping("/{id}/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateProfile(id, request);
        return ResponseEntity.ok(response);
    }

    //비밀번호 변경 PUT /api/users/{id}/password
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserPasswordChangeRequest request
    ) {
        userService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    //회원탈퇴 DELETE /api/users/{id}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
