package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.entity.DeviceCaptureRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceCaptureRequestRepository extends JpaRepository<DeviceCaptureRequest, Long> {

    Optional<DeviceCaptureRequest> findByRequestId(String requestId);
}
