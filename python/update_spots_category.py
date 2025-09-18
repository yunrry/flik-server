import mysql.connector
from mysql.connector import Error
import os
from dotenv import load_dotenv
import logging
import asyncio
import aiohttp
from concurrent.futures import ThreadPoolExecutor
from typing import Dict, List, Any, Optional
import time

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class CategoryMapper:
    """카테고리 매핑 클래스"""
    
    # MainCategory -> SubCategory 매핑
    CATEGORY_MAPPING = {
        "NATURE": [
            "자연경관(산)", "자연경관(하천‧해양)", "자연생태", "자연공원", 
            "산사체험", "기타자연관광"
        ],
        "INDOOR": [
            "전시시설", "공연시설", "교육시설", "쇼핑몰", "백화점", "대형마트"
        ],
        "HISTORY_CULTURE": [
            "역사유적지", "역사유물", "종교성지", "안보관광지", "기타문화관광지",
            "랜드마크관광", "도시공원", "도시.지역문화관광", "산업관광"
        ],
        "CAFE": [
            "카페/ 찻집"
        ],
        "ACTIVITY": [
            "육상레저스포츠", "수상레저스포츠", "항공레저스포츠", "전통체험",
            "공예체험", "농.산.어촌체험", "웰니스관광", "복합레저스포츠",
            "레저스포츠시설", "기타체험"
        ],
        "FESTIVAL": [
            "축제", "공연", "행사"
        ],
        "MARKET": [
            "시장", "전문매장/상가", "면세점", "기타쇼핑시설"
        ],
        "THEMEPARK": [
            "테마공원", "복합관광시설"
        ],
        "RESTAURANT": [
            "한식", "외국식", "간이음식", "주점"
        ],
        "ACCOMMODATION": [
            "호텔", "콘도미니엄", "펜션/민박", "모텔", "캠핑", "호스텔"
        ]
    }
    
    @classmethod
    def get_main_category(cls, label_depth2: str) -> Optional[str]:
        """labelDepth2로 MainCategory 찾기"""
        if not label_depth2:
            return None
            
        for main_category, sub_categories in cls.CATEGORY_MAPPING.items():
            if label_depth2 in sub_categories:
                return main_category
        
        return None

class SpotsCategoryUpdater:
    """spots 테이블의 category 필드 업데이트 클래스"""
    
    def __init__(self, db_config: Dict[str, str], batch_size: int = 1000, max_workers: int = 10):
        self.db_config = db_config
        self.batch_size = batch_size
        self.max_workers = max_workers
        self.category_mapper = CategoryMapper()
        self.updated_count = 0
        self.error_count = 0
    
    def get_spots_data(self) -> List[Dict[str, Any]]:
        """spots 테이블에서 label_depth2가 있는 데이터 조회"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor(dictionary=True)
            
            query = """
            SELECT id, label_depth2, category 
            FROM spots 
            WHERE label_depth2 IS NOT NULL 
            AND label_depth2 != ''
            ORDER BY id
            """
            cursor.execute(query)
            data = cursor.fetchall()
            
            logger.info(f"spots 테이블에서 {len(data)}건 조회")
            return data
            
        except Error as e:
            logger.error(f"데이터 조회 실패: {e}")
            return []
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def update_category_batch(self, spots_batch: List[Dict[str, Any]]) -> bool:
        """배치 단위로 category 업데이트"""
        if not spots_batch:
            return True
        
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            update_query = """
            UPDATE spots 
            SET category = %s 
            WHERE id = %s
            """
            
            update_data = []
            for spot in spots_batch:
                label_depth2 = spot.get('label_depth2', '')
                main_category = self.category_mapper.get_main_category(label_depth2)
                
                if main_category:
                    update_data.append((main_category, spot['id']))
                else:
                    logger.debug(f"매핑되지 않은 label_depth2: {label_depth2}")
            
            if update_data:
                cursor.executemany(update_query, update_data)
                connection.commit()
                
                batch_updated = len(update_data)
                self.updated_count += batch_updated
                logger.info(f"배치 업데이트 완료: {batch_updated}건")
                
                return True
            else:
                logger.warning("업데이트할 데이터가 없습니다.")
                return True
                
        except Error as e:
            logger.error(f"배치 업데이트 실패: {e}")
            self.error_count += len(spots_batch)
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def process_batches_parallel(self, spots_data: List[Dict[str, Any]]) -> bool:
        """병렬로 배치 처리"""
        if not spots_data:
            logger.warning("처리할 데이터가 없습니다.")
            return True
        
        # 배치로 데이터 분할
        batches = [
            spots_data[i:i + self.batch_size] 
            for i in range(0, len(spots_data), self.batch_size)
        ]
        
        logger.info(f"총 {len(batches)}개 배치로 병렬 처리 시작 (워커: {self.max_workers}개)")
        
        # ThreadPoolExecutor로 병렬 처리
        with ThreadPoolExecutor(max_workers=self.max_workers) as executor:
            futures = [
                executor.submit(self.update_category_batch, batch) 
                for batch in batches
            ]
            
            # 모든 작업 완료 대기
            results = []
            for i, future in enumerate(futures):
                try:
                    result = future.result(timeout=300)  # 5분 타임아웃
                    results.append(result)
                    logger.info(f"배치 {i+1}/{len(batches)} 완료")
                except Exception as e:
                    logger.error(f"배치 {i+1} 처리 실패: {e}")
                    results.append(False)
        
        success_count = sum(1 for result in results if result)
        logger.info(f"병렬 처리 완료: {success_count}/{len(batches)} 배치 성공")
        
        return success_count == len(batches)
    
    def update_categories(self) -> bool:
        """카테고리 업데이트 실행"""
        logger.info("spots 테이블 카테고리 업데이트 시작")
        start_time = time.time()
        
        try:
            # 1. spots 데이터 조회
            spots_data = self.get_spots_data()
            if not spots_data:
                logger.warning("업데이트할 데이터가 없습니다.")
                return False
            
            # 2. 병렬로 배치 처리
            success = self.process_batches_parallel(spots_data)
            
            elapsed_time = time.time() - start_time
            
            if success:
                logger.info(f"✅ 카테고리 업데이트 완료!")
                logger.info(f"�� 처리 결과: {self.updated_count}건 업데이트, {self.error_count}건 오류")
                logger.info(f"⏱️ 소요 시간: {elapsed_time:.2f}초")
            else:
                logger.error("❌ 카테고리 업데이트 실패")
            
            return success
            
        except Exception as e:
            logger.error(f"카테고리 업데이트 중 오류 발생: {str(e)}")
            return False

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
    
    # 카테고리 업데이트 실행
    updater = SpotsCategoryUpdater(
        db_config=db_config,
        batch_size=1000,  # 배치 크기
        max_workers=10    # 병렬 워커 수
    )
    
    updater.update_categories()

if __name__ == "__main__":
    main()