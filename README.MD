ERD 정리 (지금 버전 기준)
📌 엔티티 목록

users – 사용자

plants_info – 식물 기본 정보

devices – 라즈베리파이/키트 장치

user_plants – 사용자가 실제로 기르는 식물(내 화분)

diary – user_plants별 다이어리

sensor_logs – 장치별 센서 기록(온도, 습도, 조도, CO₂, EC)

🧱 테이블별 핵심 구조
1) users
   컬럼	타입	설명
   id	BIGSERIAL PK	사용자 ID
   email	VARCHAR(255)	로그인용 이메일, UNIQUE
   password	VARCHAR(255)	해시된 비밀번호
   nickname	VARCHAR(100)	닉네임
   job	VARCHAR(100)	직업
   age	INT	나이
   gender	VARCHAR(20)	성별
   created_at	TIMESTAMP	가입일
2) plants_info
   컬럼	타입	설명
   id	BIGSERIAL PK	식물 기본 정보 ID
   name	VARCHAR(100)	식물 이름
   scientific_name	VARCHAR(150)	학명
   difficulty	VARCHAR(20)	재배 난이도(EASY/MEDIUM…)
   temp_min/max	NUMERIC(4,1)	권장 온도 범위
   humidity_min/max	NUMERIC(4,1)	권장 습도 범위
   light_level	VARCHAR(50)	빛/조도 수준
   nutrient_cycle_days	SMALLINT	양액/물 주기(일)
   co2_min/max	NUMERIC(6,1)	권장 CO₂ 범위
   ec_min/max	NUMERIC(4,2)	권장 EC 범위
   created_at	TIMESTAMP	생성일
3) devices
   컬럼	타입	설명
   id	BIGSERIAL PK	장치 ID
   user_id	BIGINT FK→users	장치를 등록한 유저(옵션)
   device_name	VARCHAR(100)	장치 이름(앱에 보여줄 이름)
   serial_no	VARCHAR(100)	라즈베리파이 고유번호, UNIQUE
   location	VARCHAR(255)	설치 위치(베란다, 방2…)
   created_at	TIMESTAMP	등록일
4) user_plants
   컬럼	타입	설명
   id	BIGSERIAL PK	내 식물 ID
   user_id	BIGINT FK→users	이 식물을 기르는 사용자
   plant_info_id	BIGINT FK→plants_info	어떤 종류의 식물인지(기본 정보)
   device_id	BIGINT FK→devices	어떤 장치에 연결되어 있는지 (1:1, UNIQUE)
   nickname	VARCHAR(100)	사용자가 붙인 별칭
   started_at	DATE	기르기 시작한 날짜
   location	VARCHAR(255)	위치(베란다 선반 2층 등)
   memo	TEXT	메모
   created_at	TIMESTAMP	생성일

device_id에 UNIQUE 제약이 있어서
👉 식물 1개당 장치 1개, 장치 1개당 식물 1개(1:1) 구조가 됩니다.

5) diary
   컬럼	타입	설명
   id	BIGSERIAL PK	다이어리 ID
   user_plant_id	BIGINT FK→user_plants	어느 식물에 대한 다이어리인지
   diary_date	DATE	날짜
   content	TEXT	내용(메모)
   image_url	VARCHAR(500)	사진 경로(URL)
   created_at	TIMESTAMP	작성일
   updated_at	TIMESTAMP	수정일

(user_plant_id, diary_date) 에 UNIQUE 인덱스
👉 같은 식물/같은 날짜에는 다이어리 1개만 존재

6) sensor_logs
   컬럼	타입	설명
   id	BIGSERIAL PK	센서 로그 ID
   device_id	BIGINT FK→devices	어느 장치의 센서값인지
   temperature	NUMERIC(4,1)	온도
   humidity	NUMERIC(4,1)	습도
   illuminance	NUMERIC(10,2)	조도
   co2	NUMERIC(10,2)	CO₂
   ec	NUMERIC(6,2)	EC
   created_at	TIMESTAMP	기록 시간
   🔗 관계(관계도 느낌으로 정리)

User(1) ─ (N) Device

User(1) ─ (N) UserPlant

PlantInfo(1) ─ (N) UserPlant

Device(1) ─ (1) UserPlant (UNIQUE device_id)

UserPlant(1) ─ (N) Diary

Device(1) ─ (N) SensorLog

실제 ERD 그림으로 그리면 이런 형태야:

User ───< Device
│
└───< UserPlant >── PlantInfo
│
└───< Diary

Device ───< SensorLog
UserPlant —(1:1)— Device