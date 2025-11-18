package kr.ac.hansung.cse.minifarm.dto.diagnosis;

import lombok.Builder;
import lombok.Getter;


//라즈베리파이 요청 결과 폴링 dto
@Getter
@Builder
public class DeviceDiagnosisResultResponse {

    private String status;              // WAITING / PROCESSING / COMPLETED / TIMEOUT / FAILED
    private String message;             // 안내 메시지 (선택)
    private DiagnosisResponse diagnosis;
}
