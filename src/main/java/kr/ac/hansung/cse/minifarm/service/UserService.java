package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.*;
import kr.ac.hansung.cse.minifarm.entity.User;
import kr.ac.hansung.cse.minifarm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

//User 관련 비즈니스로직 회원가입/로그인/프로필 조회,수정/비밀번호 변경/ 회원탈퇴
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원가입
     * - 이메일 중복 체크
     * - 비밀번호/확인 일치 확인
     * - 비밀번호 해시 후 저장
     * - 직업/나이/성별은 선택 입력 (null 허용)
     */
    @Transactional
    public UserProfileResponse signup(UserSignupRequest request) {
        // 비밀번호 확인 체크
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 비밀번호 해시
        String hashedPassword = PasswordUtil.hashPassword(request.getPassword());

        User user = User.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(hashedPassword)
                .job(request.getJob())         // null 가능
                .age(request.getAge())         // null 가능
                .gender(request.getGender())   // null 가능
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    /**
     * 로그인
     * - 이메일로 사용자 조회
     * - 입력 비밀번호 해시 후 저장된 값과 비교
     */
    @Transactional(readOnly = true)
    public UserProfileResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        String hashedInput = PasswordUtil.hashPassword(request.getPassword());
        if (!user.getPassword().equals(hashedInput)) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 실제 서비스라면 여기서 JWT 토큰을 발급해도 됨
        return toProfileResponse(user);
    }

    /**
     * 프로필 조회
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = findUserById(userId);
        return toProfileResponse(user);
    }

    /**
     * 프로필 수정 (닉네임/직업/나이/성별)
     * - null이 아닌 필드만 업데이트 (PATCH 느낌)
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = findUserById(userId);

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getJob() != null) {
            user.setJob(request.getJob());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        return toProfileResponse(user);
    }

    /**
     * 비밀번호 변경
     * - 현재 비밀번호 검증
     * - 새 비밀번호/확인 일치 여부 확인
     * - 저장
     */
    @Transactional
    public void changePassword(Long userId, UserPasswordChangeRequest request) {
        User user = findUserById(userId);

        // 현재 비밀번호 검증
        String currentHashed = PasswordUtil.hashPassword(request.getCurrentPassword());
        if (!user.getPassword().equals(currentHashed)) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호 확인 체크
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호와 확인이 일치하지 않습니다.");
        }

        // 새 비밀번호 해시 후 저장
        String newHashed = PasswordUtil.hashPassword(request.getNewPassword());
        user.setPassword(newHashed);
    }

    /**
     * 회원탈퇴
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    // ===== 내부 유틸 메서드들 =====

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .job(user.getJob())
                .age(user.getAge())
                .gender(user.getGender())
                .build();
    }
}
