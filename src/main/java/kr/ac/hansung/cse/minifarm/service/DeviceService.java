package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.DeviceDtos.CreateReq;
import kr.ac.hansung.cse.minifarm.model.Device;
import kr.ac.hansung.cse.minifarm.model.User;
import kr.ac.hansung.cse.minifarm.repository.DeviceRepository;
import kr.ac.hansung.cse.minifarm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository devices;
    private final UserRepository users;

    public Device create(CreateReq req) {
        User owner = users.findByEmail(req.getOwnerEmail())
                .orElseThrow(() -> new IllegalArgumentException("owner not found"));
        Device d = Device.builder()
                .deviceUid(req.getDeviceUid())
                .name(req.getName())
                .location(req.getLocation())
                .owner(owner)
                .isOnline(false)
                .build();
        return devices.save(d);
    }
}
