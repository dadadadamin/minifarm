package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.entity.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// sensor_logs 테이블용 JPA 리포지토리

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {

    // 특정 기기의 가장 최근 로그 1건
    Optional<SensorLog> findFirstByDevice_IdOrderByCreatedAtDesc(Long deviceId);

    // 특정 기기의 지정 구간 로그 (시간 오름차순)
    List<SensorLog> findByDevice_IdAndCreatedAtBetweenOrderByCreatedAtAsc(
            Long deviceId,
            LocalDateTime from,
            LocalDateTime to
    );
}
