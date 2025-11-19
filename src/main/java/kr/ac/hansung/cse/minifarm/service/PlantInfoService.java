package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.PlantInfoResponse;
import kr.ac.hansung.cse.minifarm.entity.PlantInfo;
import kr.ac.hansung.cse.minifarm.repository.PlantInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantInfoService {

    private final PlantInfoRepository plantInfoRepository;

    // 전체 식물 리스트
    public List<PlantInfoResponse> getAllPlants() {
        return plantInfoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 단일 식물 상세
    public PlantInfoResponse getPlant(Long plantId) {
        PlantInfo plant = plantInfoRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식물입니다. id=" + plantId));
        return toResponse(plant);
    }

    private PlantInfoResponse toResponse(PlantInfo p) {
        return PlantInfoResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .difficulty(p.getDifficulty())
                .tempMin(p.getTempMin())
                .tempMax(p.getTempMax())
                .humidityMin(p.getHumidityMin())
                .humidityMax(p.getHumidityMax())
                .co2Min(p.getCo2Min())
                .co2Max(p.getCo2Max())
                .lightLevel(p.getLightLevel())
                // 아직 컬럼 없으면 null / 기본 텍스트
                .ledInfo("기본 LED 정보")
                .ecMin(p.getEcMin())
                .ecMax(p.getEcMax())
                .build();
    }
}
