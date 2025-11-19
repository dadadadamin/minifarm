package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.entity.Device;
import kr.ac.hansung.cse.minifarm.entity.SensorLog;
import kr.ac.hansung.cse.minifarm.mqtt.TelemetryMessageHandler.TelemetryMessage;
import kr.ac.hansung.cse.minifarm.repository.SensorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * MQTT Telemetry -> sensor_logs 저장 담당 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensorLogService {

    private final SensorLogRepository sensorLogRepository;

    public void saveTelemetry(TelemetryMessage telemetry) {

        Long deviceId = telemetry.getDeviceId();
        var m = telemetry.getMetrics();

        Device deviceRef = new Device();
        deviceRef.setId(deviceId);

        // SensorLog 객체 이름 변경 (log → sensorLog)
        SensorLog sensorLog = new SensorLog();
        sensorLog.setDevice(deviceRef);
        sensorLog.setTemperature(m.getTemperature());
        sensorLog.setHumidity(m.getHumidity());
        sensorLog.setIlluminance(m.getIlluminance());
        sensorLog.setCo2(m.getCo2());
        sensorLog.setEc(m.getEc());
        sensorLog.setCreatedAt(LocalDateTime.now());

        sensorLogRepository.save(sensorLog);

        // 로그는 logger.log() 형태로 찍어야 함
        log.debug("sensor_logs saved: deviceId={}, temp={}, hum={}, lux={}",
                deviceId,
                m.getTemperature(),
                m.getHumidity(),
                m.getIlluminance()
        );
    }
}
