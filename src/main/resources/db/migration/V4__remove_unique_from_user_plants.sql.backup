-- user_plants.device_id에 걸린 UNIQUE 인덱스 제거
DROP INDEX IF EXISTS uq_user_plants_device;

-- 필요하면 일반 인덱스로 다시 생성 (선택)
CREATE INDEX IF NOT EXISTS idx_user_plants_device ON user_plants(device_id);
