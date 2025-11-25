// src/main/java/kr/ac/hansung/cse/minifarm/dto/device/DeviceCardResponse.java
package kr.ac.hansung.cse.minifarm.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeviceCardResponse {

    private Long deviceId;          // devices.id
    private String deviceName;      // devices.device_name
    private String location;        // devices.location
    private LocalDateTime createdAt;// devices.created_at

    // 필요하면 여기에 plantCount, status 등 나중에 추가하면 됨
}
