package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.UserPlantCreateRequest;
import kr.ac.hansung.cse.minifarm.dto.UserPlantResponse;
import kr.ac.hansung.cse.minifarm.service.UserPlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-plants")
public class UserPlantController {

    private final UserPlantService userPlantService;

    // 식물 선택해서 내 식물로 등록
    // 예: POST /api/user-plants?userId=1
    @PostMapping
    public UserPlantResponse createUserPlant(
            @RequestParam Long userId,
            @RequestBody UserPlantCreateRequest request
    ) {
        return userPlantService.createUserPlant(userId, request);
    }
}
