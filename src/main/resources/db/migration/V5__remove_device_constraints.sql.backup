-- V5__remove_device_relations.sql
-- 목적: devices 테이블에 대한 FK/참조 제거 + user_plants에서 device_id 컬럼 삭제

-- 1) user_plants.device_id 관련 FK/컬럼/인덱스 제거

-- FK 제거 (PostgreSQL 기본 네이밍 가정: user_plants_device_id_fkey)
ALTER TABLE user_plants
    DROP CONSTRAINT IF EXISTS user_plants_device_id_fkey;

-- 유니크 인덱스 제거
DROP INDEX IF EXISTS uq_user_plants_device;

-- 일반 인덱스 제거 (예전에 만들어둔 경우 방지)
DROP INDEX IF EXISTS idx_user_plants_device;

-- 컬럼 자체 제거 (앱 플로우에서 더 이상 device_id 사용 안 함)
ALTER TABLE user_plants
    DROP COLUMN IF EXISTS device_id;


-- 2) sensor_logs.device_id 의 FK만 제거 (컬럼은 일단 남김, 나중에 재사용 가능)

ALTER TABLE sensor_logs
    DROP CONSTRAINT IF EXISTS sensor_logs_device_id_fkey;

-- 필요하면 NOT NULL 도 풀어서 완전 옵션 컬럼으로 (선택 사항)
ALTER TABLE sensor_logs
    ALTER COLUMN device_id DROP NOT NULL;


-- 3) device_capture_requests.device_id FK 제거 (역시 컬럼은 보존)

ALTER TABLE device_capture_requests
    DROP CONSTRAINT IF EXISTS device_capture_requests_device_id_fkey;

-- 마찬가지로 NOT NULL 해제 (선택)
ALTER TABLE device_capture_requests
    ALTER COLUMN device_id DROP NOT NULL;
