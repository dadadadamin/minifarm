package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.UserPlantCreateRequest;
import kr.ac.hansung.cse.minifarm.dto.UserPlantResponse;
import kr.ac.hansung.cse.minifarm.entity.PlantInfo;
import kr.ac.hansung.cse.minifarm.entity.User;
import kr.ac.hansung.cse.minifarm.entity.UserPlant;
import kr.ac.hansung.cse.minifarm.entity.Device;
import kr.ac.hansung.cse.minifarm.repository.DeviceRepository;
import kr.ac.hansung.cse.minifarm.repository.PlantInfoRepository;
import kr.ac.hansung.cse.minifarm.repository.UserPlantRepository;
import kr.ac.hansung.cse.minifarm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserPlantService {

    private final UserRepository userRepository;
    private final PlantInfoRepository plantInfoRepository;
    private final UserPlantRepository userPlantRepository;
    @Transactional
    public UserPlantResponse createUserPlant(Long userId, UserPlantCreateRequest request) {
        //1)사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));
        //식물 기본 정보 조회
        PlantInfo plantInfo = plantInfoRepository.findById(request.getPlantId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식물입니다. id=" + request.getPlantId()));
        //시작일 기본값: 요청 없으면 오늘로
        LocalDate startedAt = request.getStartedAt() != null
                ? request.getStartedAt()
                : LocalDate.now();
/*
        // [추가] 기기 정보 조회 및 설정
        // 프론트에서 보낸 가짜 ID(예: 1)가 실제 DB에도 존재해야 오류가 안 납니다.
        if (request.getDeviceId() == null) {
            throw new IllegalArgumentException("장치 ID는 필수입니다.");
        }
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장치입니다. id=" + request.getDeviceId()));
*/
/*
        UserPlant userPlant = UserPlant.builder()
                .user(user)
                .plantInfo(plantInfo)
                .nickname(request.getNickname())
                .startedAt(startedAt)
                .createdAt(LocalDateTime.now())
                .build();
*/
        //userplant생성 
        UserPlant userPlant = UserPlant.builder()
                .user(user)
                .plantInfo(plantInfo)
                .nickname(request.getNickname())
                .startedAt(startedAt)
                .createdAt(LocalDateTime.now())
                .build();
        UserPlant saved = userPlantRepository.save(userPlant);
        
        //응답 dto 변환
        return UserPlantResponse.builder()
                .id(saved.getId())
                .plantName(plantInfo.getName())
                .nickname(saved.getNickname())
                .startedAt(saved.getStartedAt())
                .build();
    }
}
