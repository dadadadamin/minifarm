package kr.ac.hansung.cse.minifarm.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class DeviceDtos {
    @Getter @Setter
    public static class CreateReq {
        @NotBlank private String ownerEmail;
        @NotBlank private String deviceUid;
        @NotBlank private String name;
        private String location;
    }
}
