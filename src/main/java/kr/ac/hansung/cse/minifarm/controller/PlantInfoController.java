package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.PlantInfoResponse;
import kr.ac.hansung.cse.minifarm.service.PlantInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plants")
public class PlantInfoController {

    private final PlantInfoService plantInfoService;

    // 재배할 식물 선택 화면 리스트
    @GetMapping
    public List<PlantInfoResponse> getAllPlants() {
        return plantInfoService.getAllPlants();
    }

    // 팝업 상세 정보
    @GetMapping("/{plantId}")
    public PlantInfoResponse getPlant(@PathVariable Long plantId) {
        return plantInfoService.getPlant(plantId);
    }
}
