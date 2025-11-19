// src/main/java/kr/ac/hansung/cse/minifarm/service/DeviceService.java
package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.device.DeviceCardResponse;
import kr.ac.hansung.cse.minifarm.entity.Device;
import kr.ac.hansung.cse.minifarm.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    /**
     * 특정 사용자(userId)가 소유한 스마트팟(장치) 목록 조회
     */
    public List<DeviceCardResponse> getDevicesByUser(Long userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);

        return devices.stream()
                .map(device -> DeviceCardResponse.builder()
                        .deviceId(device.getId())
                        .deviceName(device.getDeviceName())
                        .location(device.getLocation())
                        .createdAt(device.getCreatedAt())
                        .build()
                )
                .toList();
    }
}
