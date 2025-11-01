package kr.ac.hansung.cse.minifarm.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class ReadingDtos {
    @Getter @Setter
    public static class IngestReq {
        @NotBlank private String deviceUid;
        @NotBlank private String type;
        @NotNull  private Double value;
    }
}
