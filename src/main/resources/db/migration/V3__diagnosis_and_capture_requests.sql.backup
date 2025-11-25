-- AI 진단 결과 저장 테이블
CREATE TABLE diagnosis (
                           id               BIGSERIAL PRIMARY KEY,
                           user_plant_id    BIGINT NOT NULL REFERENCES user_plants(id) ON DELETE CASCADE,
                           image_url        VARCHAR(500),
                           health_summary   TEXT,
                           disease_status   VARCHAR(50),            -- NORMAL / WARNING / DANGER 등
                           disease_details  TEXT,                   -- JSON or 상세 설명 문자열
                           advice           TEXT,                   -- 관리 팁
                           harvest_prediction_date DATE,            -- 수확 시기 예측
                           source_type      VARCHAR(20) NOT NULL,   -- DEVICE / MOBILE
                           created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 라즈베리파이 사진 촬영 요청 상태 관리 테이블
CREATE TABLE device_capture_requests (
                                         id               BIGSERIAL PRIMARY KEY,
                                         request_id       VARCHAR(100) NOT NULL UNIQUE,   -- 외부에서 쓰는 UUID
                                         user_plant_id    BIGINT NOT NULL REFERENCES user_plants(id) ON DELETE CASCADE,
                                         device_id        BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                                         status           VARCHAR(20) NOT NULL,           -- WAITING / RECEIVED / PROCESSING / COMPLETED / FAILED / TIMEOUT
                                         diagnosis_id     BIGINT REFERENCES diagnosis(id) ON DELETE SET NULL,
                                         image_url        VARCHAR(500),
                                         error_message    TEXT,
                                         created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at       TIMESTAMP
);

CREATE INDEX idx_device_capture_requests_request_id
    ON device_capture_requests(request_id);

CREATE INDEX idx_device_capture_requests_status
    ON device_capture_requests(status);
