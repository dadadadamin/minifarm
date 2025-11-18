package kr.ac.hansung.cse.minifarm.dto.diagnosis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

//라즈베리파이 촬영 요청 dto
@Getter
@Builder
@AllArgsConstructor
public class DeviceDiagnosisRequestResponse {

    private String requestId;
    private int timeoutSec;
}
