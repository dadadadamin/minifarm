package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.DeviceDtos.CreateReq;
import kr.ac.hansung.cse.minifarm.model.Device;
import kr.ac.hansung.cse.minifarm.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/devices") @RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping
    public Device create(@Valid @RequestBody CreateReq req){
        return deviceService.create(req);
    }
}
