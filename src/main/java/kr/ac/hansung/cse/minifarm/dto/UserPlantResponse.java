package kr.ac.hansung.cse.minifarm.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class UserPlantResponse {

    private Long id;             // user_plants ID
    private String plantName;    // ex)상추
    private String nickname;     // 별칭
    private LocalDate startedAt;
}
