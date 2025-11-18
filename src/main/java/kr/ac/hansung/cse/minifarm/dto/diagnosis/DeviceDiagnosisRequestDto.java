package kr.ac.hansung.cse.minifarm.dto.diagnosis;

import lombok.Getter;
import lombok.Setter;


//라즈베리파이 촬영 요청 dto
@Getter
@Setter
public class DeviceDiagnosisRequestDto {

    private Long userPlantId;
    private String symptomNote;   // 선택 입력 (잎이 노래요 등)
}
