CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     email TEXT NOT NULL UNIQUE,
                                     password_hash TEXT NOT NULL,
                                     nickname TEXT,
                                     created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS devices (
                                       id BIGSERIAL PRIMARY KEY,
                                       device_uid TEXT NOT                                       location TEXT,
                                       is_online BOOLEAN DEFAULT FALSE,
                                       owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sensor_logs (
                                           id BIGSERIAL PRIMARY KEY,
                                           device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                                           sensor_type TEXT NOT NULL,                  -- temperature/humidity/lux/soil_moisture
                                           sensor_value DOUBLE PRECISION NOT NULL,
                                           recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                           slot_no INT,
                                           meta JSONB
);

CREATE INDEX IF NOT EXISTS idx_sensor_logs_device_type_ts
    ON sensor_logs(device_id, sensor_type, recorded_at DESC);
