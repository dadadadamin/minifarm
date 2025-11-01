package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.ReadingDtos.IngestReq;
import kr.ac.hansung.cse.minifarm.model.SensorLog;
import kr.ac.hansung.cse.minifarm.repository.SensorLogRepository;
import kr.ac.hansung.cse.minifarm.service.ReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController @RequestMapping("/readings") @RequiredArgsConstructor
public class ReadingController {
    private final ReadingService readingService;
    private final SensorLogRepository logs;

    @PostMapping("/ingest")
    public SensorLog ingest(@Valid @RequestBody IngestReq req){
        return readingService.ingest(req);
    }

    @GetMapping("/device/{deviceId}")
    public List<SensorLog> range(@PathVariable Long deviceId,
                                 @RequestParam String from,
                                 @RequestParam String to){
        return logs.findRange(deviceId, OffsetDateTime.parse(from), OffsetDateTime.parse(to));
    }
}

