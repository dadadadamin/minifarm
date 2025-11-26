-- V2__insert_default_plants.sql
-- 기본 식물 정보 초기 데이터 삽입
--
-- 추후 식물사진(대표사진),description등 컬럼 추가해서 develop예정

INSERT INTO plants_info
(name, scientific_name, difficulty, temp_min, temp_max, humidity_min, humidity_max,
 light_level, nutrient_cycle_days, co2_min, co2_max, ec_min, ec_max, created_at)
VALUES
    ('상추', 'Lactuca sativa', 'EASY', 15, 20, 60, 70, 'MEDIUM', 2, 400, 600, 0.8, 1.5, NOW()),
    ('바질', 'Ocimum basilicum', 'EASY', 20, 25, 50, 60, 'HIGH', 3, 400, 600, 1.0, 2.0, NOW()),
    ('방울토마토', 'Solanum lycopersicum var. cerasiforme', 'EASY', 20, 28, 50, 60, 'HIGH', 1, 400, 700, 1.5, 2.5, NOW()),
    ('선인장', 'Cactaceae', 'EASY', 10, 35, 20, 40, 'HIGH', 21, 300, 500, 0.5, 1.0, NOW()),

    ('딸기', 'Fragaria ananassa', 'MEDIUM', 18, 25, 55, 65, 'HIGH', 3, 600, 800, 1.8, 2.5, NOW()),
    ('파프리카', 'Capsicum annuum', 'MEDIUM', 20, 28, 60, 70, 'HIGH', 2, 500, 700, 1.5, 2.5, NOW()),
    ('레몬', 'Citrus limon', 'MEDIUM', 15, 28, 50, 60, 'HIGH', 4, 400, 600, 1.5, 2.5, NOW()),

    ('수박', 'Citrullus lanatus', 'HARD', 25, 30, 50, 60, 'HIGH', 1, 700, 1000, 2.0, 3.0, NOW()),
    ('분재 소나무', 'Pinus spp.', 'HARD', 5, 25, 40, 60, 'HIGH', 2, 400, 600, 0.8, 1.5, NOW());
