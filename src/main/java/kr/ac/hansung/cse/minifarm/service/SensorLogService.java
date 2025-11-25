package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.entity.Device;
import kr.ac.hansung.cse.minifarm.entity.SensorLog;
import kr.ac.hansung.cse.minifarm.entity.UserPlant;
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

        Long userPlantId = telemetry.getUserPlantId();
        var m = telemetry.getMetrics();


        UserPlant userPlantRef =new UserPlant();
        userPlantRef.setId(userPlantId);
        /*매번 db 찾아가기 싫으면 아래로 바꾸기
        UserPlant userPlantRef = UserPlantRepository.getReferenceById(userPlantId);
                SensorLog sensorLog = SensorLog.builder()
                .userPlant(userPlantRef)
                .temperature(m.getTemperature())
                .humidity(m.getHumidity())
                .illuminance(m.getIlluminance())
                .co2(m.getCo2())
                .ec(m.getEc())
                .createdAt(LocalDateTime.now())
                .build();

        sensorLogRepository.save(sensorLog);
*/
        // SensorLog 객체 이름 변경 (log → sensorLog)
        SensorLog sensorLog = new SensorLog();
        sensorLog.setUserPlant(userPlantRef);
        sensorLog.setTemperature(m.getTemperature());
        sensorLog.setHumidity(m.getHumidity());
        sensorLog.setIlluminance(m.getIlluminance());
        sensorLog.setCo2(m.getCo2());
        sensorLog.setEc(m.getEc());
        sensorLog.setCreatedAt(LocalDateTime.now());

        sensorLogRepository.save(sensorLog);

        // 로그는 logger.log() 형태로 찍어야 함
        log.debug("sensor_logs saved: userPlantId={}, temp={}, hum={}, lux={}",
                userPlantId,
                m.getTemperature(),
                m.getHumidity(),
                m.getIlluminance()
        );
    }
}
