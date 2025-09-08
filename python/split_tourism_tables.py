import mysql.connector
from mysql.connector import Error
import os
from dotenv import load_dotenv
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class TourismTableSplitter:
    def __init__(self, db_config):
        self.db_config = db_config
        self.table_mapping = {
            "12": "fetched_tourist_attractions",  # 관광지
            "14": "fetched_cultural_facilities",  # 문화시설
            "15": "fetched_festivals_events",     # 축제공연행사
            "28": "fetched_sports_recreation",    # 레포츠
            "32": "fetched_accommodations",       # 숙박
            "38": "fetched_shopping",            # 쇼핑
            "39": "fetched_restaurants"          # 음식점
        }
        
    def create_tables(self):
        """7개 테이블 생성"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            for content_type_id, table_name in self.table_mapping.items():
                create_table_query = f"""
                CREATE TABLE IF NOT EXISTS {table_name} (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    content_id VARCHAR(50) UNIQUE NOT NULL,
                    content_type_id VARCHAR(10) NOT NULL,
                    content_type_name VARCHAR(50) NOT NULL,
                    title VARCHAR(500) NOT NULL,
                    addr1 VARCHAR(500),
                    addr2 VARCHAR(500),
                    first_image TEXT,
                    first_image2 TEXT,
                    map_x VARCHAR(50),
                    map_y VARCHAR(50),
                    area_code VARCHAR(10),
                    sigungu_code VARCHAR(10),
                    cat1 VARCHAR(10),
                    cat2 VARCHAR(10),
                    cat3 VARCHAR(10),
                    created_time VARCHAR(50),
                    modified_time VARCHAR(50),
                    tel TEXT,
                    zipcode VARCHAR(20),
                    overview TEXT,
                    raw_data JSON,
                    source VARCHAR(255) DEFAULT 'http://apis.data.go.kr/B551011/KorService2',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_content_type (content_type_id),
                    INDEX idx_area (area_code, sigungu_code),
                    INDEX idx_title (title(100))
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """
                
                cursor.execute(create_table_query)
                logger.info(f"테이블 생성 완료: {table_name}")
            
            connection.commit()
            return True
            
        except Error as e:
            logger.error(f"테이블 생성 실패: {e}")
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def get_data_by_content_type(self, content_type_id):
        """특정 content_type_id에 해당하는 데이터 조회"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor(dictionary=True)
            
            query = "SELECT * FROM tourism_spots WHERE content_type_id = %s"
            cursor.execute(query, (content_type_id,))
            data = cursor.fetchall()
            
            logger.info(f"content_type_id {content_type_id} 데이터 조회: {len(data)}건")
            return data
            
        except Error as e:
            logger.error(f"데이터 조회 실패: {e}")
            return []
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def insert_data_to_table(self, table_name, data):
        """특정 테이블에 데이터 삽입"""
        if not data:
            logger.warning(f"{table_name} 테이블에 삽입할 데이터가 없습니다.")
            return True
            
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            # 데이터 삽입 쿼리
            insert_query = f"""
            INSERT INTO {table_name} (
                content_id, content_type_id, content_type_name, title, addr1, addr2,
                first_image, first_image2, map_x, map_y, area_code, sigungu_code,
                cat1, cat2, cat3, created_time, modified_time, tel, zipcode, 
                overview, raw_data, source, created_at, updated_at
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
            ) ON DUPLICATE KEY UPDATE
                content_type_id = VALUES(content_type_id),
                content_type_name = VALUES(content_type_name),
                title = VALUES(title),
                addr1 = VALUES(addr1),
                addr2 = VALUES(addr2),
                first_image = VALUES(first_image),
                first_image2 = VALUES(first_image2),
                map_x = VALUES(map_x),
                map_y = VALUES(map_y),
                area_code = VALUES(area_code),
                sigungu_code = VALUES(sigungu_code),
                cat1 = VALUES(cat1),
                cat2 = VALUES(cat2),
                cat3 = VALUES(cat3),
                created_time = VALUES(created_time),
                modified_time = VALUES(modified_time),
                tel = VALUES(tel),
                zipcode = VALUES(zipcode),
                overview = VALUES(overview),
                raw_data = VALUES(raw_data),
                source = VALUES(source),
                updated_at = CURRENT_TIMESTAMP
            """
            
            # 배치 삽입을 위한 데이터 준비
            insert_data = []
            for item in data:
                insert_data.append((
                    item.get('content_id', ''),
                    item.get('content_type_id', ''),
                    item.get('content_type_name', ''),
                    item.get('title', ''),
                    item.get('addr1', ''),
                    item.get('addr2', ''),
                    item.get('first_image', ''),
                    item.get('first_image2', ''),
                    item.get('map_x', ''),
                    item.get('map_y', ''),
                    item.get('area_code', ''),
                    item.get('sigungu_code', ''),
                    item.get('cat1', ''),
                    item.get('cat2', ''),
                    item.get('cat3', ''),
                    item.get('created_time', ''),
                    item.get('modified_time', ''),
                    item.get('tel', ''),
                    item.get('zipcode', ''),
                    item.get('overview', ''),
                    item.get('raw_data', ''),
                    item.get('source', 'http://apis.data.go.kr/B551011/KorService2'),
                    item.get('created_at'),
                    item.get('updated_at')
                ))
            
            # 배치 삽입 실행
            cursor.executemany(insert_query, insert_data)
            connection.commit()
            
            logger.info(f"{table_name} 테이블 저장 완료: {len(data)}건")
            return True
            
        except Error as e:
            logger.error(f"{table_name} 테이블 저장 실패: {e}")
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def split_tables(self):
        """tourism_spots 테이블을 7개 테이블로 분리"""
        logger.info("테이블 분리 작업 시작")
        
        # 1. 7개 테이블 생성
        if not self.create_tables():
            logger.error("테이블 생성 실패")
            return False
        
        # 2. 각 content_type_id별로 데이터 분리 및 삽입
        success_count = 0
        total_count = 0
        
        for content_type_id, table_name in self.table_mapping.items():
            logger.info(f"처리 중: {content_type_id} -> {table_name}")
            
            # 데이터 조회
            data = self.get_data_by_content_type(content_type_id)
            total_count += len(data)
            
            if data:
                # 테이블에 데이터 삽입
                if self.insert_data_to_table(table_name, data):
                    success_count += len(data)
                    logger.info(f"✅ {table_name}: {len(data)}건 처리 완료")
                else:
                    logger.error(f"❌ {table_name}: 데이터 삽입 실패")
            else:
                logger.warning(f"⚠️ {content_type_id}에 해당하는 데이터가 없습니다.")
        
        logger.info(f"테이블 분리 완료 - 총 {success_count}/{total_count}건 처리")
        return success_count == total_count
    
    def verify_split(self):
        """분리 결과 검증"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            logger.info("=== 분리 결과 검증 ===")
            
            # 원본 테이블 건수
            cursor.execute("SELECT COUNT(*) FROM tourism_spots")
            original_count = cursor.fetchone()[0]
            logger.info(f"원본 tourism_spots: {original_count}건")
            
            # 분리된 테이블들 건수
            total_split_count = 0
            for content_type_id, table_name in self.table_mapping.items():
                cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
                count = cursor.fetchone()[0]
                total_split_count += count
                logger.info(f"{table_name}: {count}건")
            
            logger.info(f"분리된 테이블 총합: {total_split_count}건")
            logger.info(f"데이터 일치 여부: {'✅ 일치' if original_count == total_split_count else '❌ 불일치'}")
            
            return original_count == total_split_count
            
        except Error as e:
            logger.error(f"검증 실패: {e}")
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()


def main():
    # .env 파일 로드
    load_dotenv()
    
    # DB 연결 정보 설정
    db_config = {
        'host': os.getenv('MYSQL_HOST', 'localhost'),
        'port': int(os.getenv('MYSQL_PORT', '3306')),
        'user': os.getenv('MYSQL_USER'),
        'password': os.getenv('MYSQL_PASSWORD'),
        'database': os.getenv('MYSQL_DATABASE'),
        'charset': 'utf8mb4',
        'autocommit': False
    }
    
    # 필수 환경변수 확인
    required_env_vars = ['MYSQL_USER', 'MYSQL_PASSWORD', 'MYSQL_DATABASE']
    missing_vars = [var for var in required_env_vars if not os.getenv(var)]
    
    if missing_vars:
        logger.error(f"필수 환경변수가 누락되었습니다: {', '.join(missing_vars)}")
        return
    
    # 테이블 분리기 초기화
    splitter = TourismTableSplitter(db_config)
    
    try:
        # 테이블 분리 실행
        if splitter.split_tables():
            logger.info("테이블 분리 성공!")
            
            # 결과 검증
            if splitter.verify_split():
                logger.info("✅ 모든 작업이 성공적으로 완료되었습니다!")
            else:
                logger.warning("⚠️ 데이터 검증에서 문제가 발견되었습니다.")
        else:
            logger.error("❌ 테이블 분리 실패")
            
    except Exception as e:
        logger.error(f"작업 중 오류 발생: {str(e)}")


if __name__ == "__main__":
    main()