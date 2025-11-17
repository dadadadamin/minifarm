package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.UserPlantCreateRequest;
import kr.ac.hansung.cse.minifarm.dto.UserPlantResponse;
import kr.ac.hansung.cse.minifarm.entity.PlantInfo;
import kr.ac.hansung.cse.minifarm.entity.User;
import kr.ac.hansung.cse.minifarm.entity.UserPlant;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));

        PlantInfo plantInfo = plantInfoRepository.findById(request.getPlantId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식물입니다. id=" + request.getPlantId()));

        LocalDate startedAt = request.getStartedAt() != null
                ? request.getStartedAt()
                : LocalDate.now();

        UserPlant userPlant = UserPlant.builder()
                .user(user)
                .plantInfo(plantInfo)
                .nickname(request.getNickname())
                .startedAt(startedAt)
                .createdAt(LocalDateTime.now())
                .build();

        UserPlant saved = userPlantRepository.save(userPlant);

        return UserPlantResponse.builder()
                .id(saved.getId())
                .plantName(plantInfo.getName())
                .nickname(saved.getNickname())
                .startedAt(saved.getStartedAt())
                .build();
    }
}
