package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.model.SensorLog;
import org.springframework.data.jpa.repository.*;
import java.time.OffsetDateTime;
import java.util.List;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {
    @Query("""
           select r from SensorLog r
           where r.device.id = :deviceId and r.recordedAt between :from and :to
           order by r.recordedAt desc
           """)
    List<SensorLog> findRange(Long deviceId, OffsetDateTime from, OffsetDateTime to);
}
