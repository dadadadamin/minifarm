-- 1. user_plants 테이블에서 device_id 관련 모두 제거
------------------------------------------------------------

-- FK 제거 (기본 이름 추정: user_plants_device_id_fkey)
ALTER TABLE user_plants
    DROP CONSTRAINT IF EXISTS user_plants_device_id_fkey;

-- 인덱스 제거 (UNIQUE / 일반 인덱스 모두)
DROP INDEX IF EXISTS idx_user_plants_device;
DROP INDEX IF EXISTS uq_user_plants_device;

-- 컬럼 제거
ALTER TABLE user_plants
    DROP COLUMN IF EXISTS device_id;

------------------------------------------------------------
-- 2. sensor_logs: device_id → user_plant_id 로 변경
------------------------------------------------------------

-- 기존 FK 제거 (기본 이름 추정: sensor_logs_device_id_fkey)
ALTER TABLE sensor_logs
    DROP CONSTRAINT IF EXISTS sensor_logs_device_id_fkey;

-- 기존 인덱스 제거
DROP INDEX IF EXISTS idx_sensor_logs_device_created_at;

-- 컬럼 이름 변경 (타입은 BIGINT로 동일)
ALTER TABLE sensor_logs
    RENAME COLUMN device_id TO user_plant_id;

-- 새 FK 추가: user_plants(id) 참조
ALTER TABLE sensor_logs
    ADD CONSTRAINT fk_sensor_logs_user_plant
        FOREIGN KEY (user_plant_id)
            REFERENCES user_plants(id)
            ON DELETE CASCADE;

-- 새 인덱스 생성 (user_plant 기준 + created_at DESC)
CREATE INDEX IF NOT EXISTS idx_sensor_logs_user_plant_created_at
    ON sensor_logs(user_plant_id, created_at DESC);

------------------------------------------------------------
-- 3. device_capture_requests: 더 이상 사용하지 않으므로 삭제
--   (라즈베리파이 촬영 요청 흐름 제거)
------------------------------------------------------------

-- 관련 인덱스 먼저 제거 (있으면)
DROP INDEX IF EXISTS idx_device_capture_requests_request_id;
DROP INDEX IF EXISTS idx_device_capture_requests_status;