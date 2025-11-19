// src/main/java/kr/ac/hansung/cse/minifarm/repository/DeviceRepository.java
package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    // Device.user.id 로 자동 조인해서 찾아줍니다.
    List<Device> findByUserId(Long userId);
}
