package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//users 테이블에 대한 JPA Repository
 //기본 CRUD + 이메일로 조회

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회 (로그인 등에 사용)
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크용 (이미 존재하는지)
    boolean existsByEmail(String email);
}
