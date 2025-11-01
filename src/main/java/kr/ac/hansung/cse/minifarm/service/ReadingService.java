package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.ReadingDtos.IngestReq;
import kr.ac.hansung.cse.minifarm.model.Device;
import kr.ac.hansung.cse.minifarm.model.SensorLog;
import kr.ac.hansung.cse.minifarm.repository.DeviceRepository;
import kr.ac.hansung.cse.minifarm.repository.SensorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service @RequiredArgsConstructor
public class ReadingService {
    private final DeviceRepository devices;
    private final SensorLogRepository logs;

    public SensorLog ingest(IngestReq req){
        Device device = devices.findByDeviceUid(req.getDeviceUid())
                .orElseThrow(() -> new IllegalArgumentException("device not found"));
        SensorLog r = SensorLog.builder()
                .device(device)
                .sensorType(req.getType())
                .sensorValue(req.getValue())
                .recordedAt(OffsetDateTime.now())
                .build();
        return logs.save(r);
    }
}
