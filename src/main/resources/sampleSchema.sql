USE flik_db;

CREATE TABLE IF NOT EXISTS spots (
    -- 기본 키
                       id BIGINT NOT NULL AUTO_INCREMENT,

    -- Discriminator 컬럼 (상속 구분)
                       spot_type VARCHAR(31) NOT NULL,

    -- BaseSpotEntity 공통 컬럼들
                       name VARCHAR(255) NOT NULL,
                       content_type_id VARCHAR(255) NOT NULL,
                       category VARCHAR(255) NOT NULL,
                       description TEXT,
                       address VARCHAR(255) NOT NULL,
                       regn_cd VARCHAR(255),
                       signgu_cd VARCHAR(255),
                       latitude DECIMAL(10,8),
                       longitude DECIMAL(11,8),
                       image_urls TEXT,
                       info VARCHAR(255),
                       rating DECIMAL(2,1),
                       google_place_id VARCHAR(255),
                       review_count INT,
                       tag1 VARCHAR(255),
                       tag2 VARCHAR(255),
                       tag3 VARCHAR(255),
                       tags VARCHAR(255),
                       parking VARCHAR(255),
                       pet_carriage VARCHAR(255),
                       baby_carriage VARCHAR(255),
                       open_time TIME,
                       close_time TIME,
                       day_off VARCHAR(255),

    -- TourSpotEntity 전용 컬럼들
                       exp_guide TEXT,
                       age_limit VARCHAR(255),
                       check_in_time VARCHAR(255),
                       check_out_time VARCHAR(255),
                       cooking BOOLEAN,
                       facilities TEXT,
                       fee VARCHAR(255),
                       event_start_date VARCHAR(255),
                       event_end_date VARCHAR(255),
                       sponsor VARCHAR(255),
                       running_time VARCHAR(255),

    -- RestaurantEntity 전용 컬럼들
                       cuisine_type VARCHAR(255),
                       price_range VARCHAR(255),
                       reservation VARCHAR(255),
                       kids_facility VARCHAR(255),
                       take_away VARCHAR(255),
                       first_menu VARCHAR(255),
                       treat_menu VARCHAR(255),

    -- ShopEntity 전용 컬럼들
                       products TEXT,

    -- 기본 키 및 인덱스
                       PRIMARY KEY (id),
                       INDEX idx_spot_type (spot_type),
                       INDEX idx_category (category),
                       INDEX idx_rating (rating),
                       INDEX idx_address (address),
                       INDEX idx_location (latitude, longitude)
);


CREATE TABLE IF NOT EXISTS user_saved_spots (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  user_id BIGINT NOT NULL,
                                  spot_id BIGINT NOT NULL,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  INDEX idx_user_id_created_at (user_id, created_at DESC),
                                  INDEX idx_spot_id (spot_id),
                                  UNIQUE KEY uk_user_spot (user_id, spot_id)
);

-- 사용자 카테고리 선호도 테이블
CREATE TABLE IF NOT EXISTS user_category_preferences (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           user_id BIGINT NOT NULL,
                                           main_category VARCHAR(50) NOT NULL,
                                           sub_category VARCHAR(50) NOT NULL,
                                           detail_category VARCHAR(50) NOT NULL,
                                           preference_score DOUBLE DEFAULT 0.0,
                                           save_count INT DEFAULT 0,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                           UNIQUE KEY uk_user_category (user_id, detail_category),
                                           INDEX idx_user_id (user_id),
                                           INDEX idx_category (detail_category)
);

-- 장소별 저장 통계 테이블
CREATE TABLE IF NOT EXISTS spot_save_statistics (
                                      spot_id BIGINT PRIMARY KEY,
                                      save_count INT DEFAULT 0,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);