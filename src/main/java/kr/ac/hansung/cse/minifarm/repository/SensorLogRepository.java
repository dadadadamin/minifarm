package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.entity.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// sensor_logs 테이블용 JPA 리포지토리

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {

    Optional<SensorLog> findFirstByUserPlant_IdOrderByCreatedAtDesc(Long userPlantId);

    List<SensorLog> findByUserPlant_IdAndCreatedAtBetweenOrderByCreatedAtAsc(
            Long userPlantId,
            LocalDateTime from,
            LocalDateTime to
    );

}
