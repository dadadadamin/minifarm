CREATE TABLE users (
                       id          BIGSERIAL PRIMARY KEY,
                       email       VARCHAR(255) NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,
                       nickname    VARCHAR(100) NOT NULL,
                       job         VARCHAR(100),
                       age         INTEGER,
                       gender      VARCHAR(20),
                       created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


--식물 별 생장 환경 저장 테이블
CREATE TABLE plants_info (
                             id                  BIGSERIAL PRIMARY KEY,
                             name                VARCHAR(100) NOT NULL,
                             scientific_name     VARCHAR(150),
                             difficulty          VARCHAR(20),
                             temp_min            NUMERIC(4,1),
                             temp_max            NUMERIC(4,1),
                             humidity_min        NUMERIC(4,1),
                             humidity_max        NUMERIC(4,1),
                             light_level         VARCHAR(50),
                             nutrient_cycle_days SMALLINT,
                             co2_min             NUMERIC(6,1),
                             co2_max             NUMERIC(6,1),
                             ec_min              NUMERIC(4,2),
                             ec_max              NUMERIC(4,2),
                             created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--초기 세팅 식물관리 장치(라즈베리파이) 연결 설정
CREATE TABLE devices (
                         id          BIGSERIAL PRIMARY KEY,
                         user_id     BIGINT REFERENCES users(id),
                         device_name VARCHAR(100) NOT NULL,
                         serial_no   VARCHAR(100) NOT NULL UNIQUE,
                         location    VARCHAR(255),
                         created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--사용자가 설정한 식물
CREATE TABLE user_plants (
                             id              BIGSERIAL PRIMARY KEY,
                             user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             plant_info_id   BIGINT REFERENCES plants_info(id),
                             device_id       BIGINT NOT NULL REFERENCES devices(id),   -- NOT NULL 로 변경
                             nickname        VARCHAR(100),
                             started_at      DATE NOT NULL,
                             location        VARCHAR(255),
                             memo            TEXT,
                             created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE UNIQUE INDEX uq_user_plants_device
    ON user_plants(device_id);

--식물 별 생장 다이어리
CREATE TABLE diary (
                       id              BIGSERIAL PRIMARY KEY,
                       user_plant_id   BIGINT NOT NULL REFERENCES user_plants(id) ON DELETE CASCADE,
                       diary_date      DATE NOT NULL,
                       content         TEXT,
                       image_url       VARCHAR(500),
                       created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      TIMESTAMP
);

CREATE UNIQUE INDEX uq_diary_user_plant_date
    ON diary(user_plant_id, diary_date);


--라즈베리파이에서 실시간으로 수집해야 하는 환경 관련 데이터들
CREATE TABLE sensor_logs (
                             id           BIGSERIAL PRIMARY KEY,
                             device_id    BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                             temperature  NUMERIC(4,1),
                             humidity     NUMERIC(4,1),
                             illuminance  NUMERIC(10,2),
                             co2          NUMERIC(10,2),
                             ec           NUMERIC(6,2),
                             created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sensor_logs_device_created_at
    ON sensor_logs(device_id, created_at DESC);
