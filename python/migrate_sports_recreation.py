import mysql.connector
from mysql.connector import Error
import os
from dotenv import load_dotenv
import logging
import re
import json
from collections import Counter
from typing import List, Dict, Any

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class SportsRecreationMigrator:
    def __init__(self, db_config: Dict[str, str]):
        self.db_config = db_config
        
        # 서울 구별 매핑 정보
        self.district_mapping = {
            "종로구": "110", "중구": "140", "용산구": "170", "성동구": "200", "광진구": "215",
            "동대문구": "230", "중랑구": "260", "성북구": "290", "강북구": "305", "도봉구": "320",
            "노원구": "350", "은평구": "380", "서대문구": "410", "마포구": "440", "양천구": "470",
            "강서구": "500", "구로구": "530", "금천구": "545", "영등포구": "560", "동작구": "590",
            "관악구": "620", "서초구": "650", "강남구": "680", "송파구": "710", "강동구": "740"
        }
    
    def extract_keywords_from_reviews(self, google_reviews: str) -> List[str]:
        """Google 리뷰에서 레포츠 관련 키워드 추출"""
        if not google_reviews or google_reviews.strip() == '':
            return []
        
        try:
            reviews_data = json.loads(google_reviews)
            all_text = ""
            
            if isinstance(reviews_data, list):
                for review in reviews_data:
                    if isinstance(review, dict) and 'text' in review:
                        all_text += review['text'] + " "
            elif isinstance(reviews_data, dict) and 'reviews' in reviews_data:
                for review in reviews_data['reviews']:
                    if isinstance(review, dict) and 'text' in review:
                        all_text += review['text'] + " "
            
            # 레포츠/운동 키워드 추출
            sports_keywords = re.findall(r'[가-힣]{2,4}(?:체육관|수영장|골프장|테니스장|배드민턴|축구장|농구장|볼링장|당구장|탁구장|헬스장|요가|필라테스|스쿼시|스케이트|등산|하이킹|자전거|런닝|조깅|마라톤|수영|다이빙|서핑|요트|카약|클라이밍|암벽등반|번지점프|패러글라이딩|행글라이딩|스카이다이빙|체력단련|운동|스포츠|레포츠|피트니스|웰니스|건강|단련|체력|근력|유연성|지구력|협응)', all_text)
            
            # 감정/평가 키워드
            emotion_keywords = re.findall(r'[가-힣]{2,4}(?:좋|나쁘|아름다|예쁘|멋있|훌륭|완벽|최고|추천|만족|불만족|감동|실망|재미있|즐겁|피곤하|힘들|쉬|어려)', all_text)
            
            # 모든 키워드 합치기
            all_keywords = sports_keywords + emotion_keywords
            
            # 빈도수 계산하여 상위 3개 반환
            keyword_counts = Counter(all_keywords)
            top_keywords = [word for word, count in keyword_counts.most_common(3)]
            
            return top_keywords
            
        except (json.JSONDecodeError, TypeError):
            return []
        except Exception as e:
            logger.warning(f"키워드 추출 실패: {e}")
            return []
    
    def parse_address_to_district(self, addr1: str) -> str:
        """주소에서 구 정보 추출하여 코드 반환"""
        if not addr1:
            return "110"  # 기본값: 종로구
        
        addr_clean = addr1.replace("서울특별시", "").replace("서울", "").strip()
        
        for district_name, district_code in self.district_mapping.items():
            if district_name in addr_clean:
                return district_code
        
        return "110"
    
    def get_sports_recreation_data(self) -> List[Dict[str, Any]]:
        """fetched_sports_recreation 테이블에서 데이터 조회"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor(dictionary=True)
            
            # label_depth1이 존재하는 데이터만 조회
            query = """
            SELECT * FROM fetched_sports_recreation 
            WHERE label_depth1 IS NOT NULL 
            AND label_depth1 != '' 
            ORDER BY id
            """
            cursor.execute(query)
            data = cursor.fetchall()
            
            logger.info(f"fetched_sports_recreation에서 label_depth1이 있는 {len(data)}건 조회")
            return data
            
        except Error as e:
            logger.error(f"데이터 조회 실패: {e}")
            return []
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def transform_to_spots_data(self, sports_data: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """fetched_sports_recreation 데이터를 spots 형식으로 변환"""
        spots_data = []
        
        for item in sports_data:
            # 주소에서 구 코드 추출
            signgu_cd = self.parse_address_to_district(item.get('addr1', ''))
            
            # 키워드 추출
            keywords = self.extract_keywords_from_reviews(item.get('google_reviews', ''))
            
            # 이미지 URL 처리
            image_urls = []
            if item.get('first_image'):
                image_urls.append(item['first_image'])
            if item.get('first_image2'):
                image_urls.append(item['first_image2'])
            
            spot_data = {
                'spot_type': 'LEISURE',
                'address': item.get('addr1', ''),
                'baby_carriage': item.get('chkbabycarriage', ''),
                'category': item.get('content_type_name', ''),
                'close_time': None,
                'content_type_id': item.get('content_type_id', ''),
                'content_id': item.get('content_id', ''),
                'day_off': item.get('restdate', ''),
                'description': item.get('overview', ''),
                'google_place_id': item.get('google_place_id', ''),
                'image_urls': json.dumps(image_urls, ensure_ascii=False) if image_urls else None,
                'info': item.get('infocenter', ''),
                'latitude': float(item.get('map_y', 0)) if item.get('map_y') else None,
                'longitude': float(item.get('map_x', 0)) if item.get('map_x') else None,
                'name': item.get('title', ''),
                'open_time': None,
                'parking': item.get('parking', ''),
                'pet_carriage': item.get('chkpet', ''),
                'rating': item.get('google_rating', ''),
                'regn_cd': '11',  # 서울특별시
                'review_count': item.get('google_review_count', ''),
                'signgu_cd': signgu_cd,
                'tag1': keywords[0] if len(keywords) > 0 else '',
                'tag2': keywords[1] if len(keywords) > 1 else '',
                'tag3': keywords[2] if len(keywords) > 2 else '',
                'tags': ','.join(keywords) if keywords else '',
                'label_depth1': item.get('label_depth1', ''),
                'label_depth2': item.get('label_depth2', ''),
                'label_depth3': item.get('label_depth3', ''),
                'time': item.get('usetime', ''),
                'reservation': item.get('reservation', ''),  # 레포츠 특화 필드
                'exp_guide': item.get('expagerangeleports', '')  # 체험연령대
            }
            
            spots_data.append(spot_data)
        
        return spots_data
    
    def insert_into_spots(self, spots_data: List[Dict[str, Any]]) -> bool:
        """spots 테이블에 데이터 삽입"""
        if not spots_data:
            return True
        
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            insert_query = """
            INSERT INTO spots (
                spot_type, address, baby_carriage, category, close_time, content_type_id, content_id,
                day_off, description, google_place_id, image_urls, info, latitude,
                longitude, name, open_time, parking, pet_carriage, rating, regn_cd,
                review_count, signgu_cd, tag1, tag2, tag3, tags, label_depth1, label_depth2, label_depth3, time, reservation, exp_guide
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
            )
            """
            
            # 배치 삽입을 위한 데이터 준비
            insert_data = []
            for item in spots_data:
                insert_data.append((
                    item.get('spot_type', ''),
                    item.get('address', ''),
                    item.get('baby_carriage', ''),
                    item.get('category', ''),
                    item.get('close_time'),
                    item.get('content_type_id', ''),
                    item.get('content_id', ''),
                    item.get('day_off', ''),
                    item.get('description', ''),
                    item.get('google_place_id', ''),
                    item.get('image_urls', ''),
                    item.get('info', ''),
                    item.get('latitude'),
                    item.get('longitude'),
                    item.get('name', ''),
                    item.get('open_time'),
                    item.get('parking', ''),
                    item.get('pet_carriage', ''),
                    item.get('rating'),
                    item.get('regn_cd', ''),
                    item.get('review_count'),
                    item.get('signgu_cd', ''),
                    item.get('tag1', ''),
                    item.get('tag2', ''),
                    item.get('tag3', ''),
                    item.get('tags', ''),
                    item.get('label_depth1', ''),
                    item.get('label_depth2', ''),
                    item.get('label_depth3', ''),
                    item.get('time', ''),
                    item.get('reservation', ''),
                    item.get('exp_guide', '')
                ))
            
            # 배치 삽입 실행
            cursor.executemany(insert_query, insert_data)
            connection.commit()
            
            logger.info(f"spots 테이블에 {len(spots_data)}건 삽입 완료")
            return True
            
        except Error as e:
            logger.error(f"spots 테이블 삽입 실패: {e}")
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def migrate_data(self) -> bool:
        """데이터 마이그레이션 실행"""
        logger.info("레포츠 데이터 마이그레이션 시작")
        
        try:
            # 1. fetched_sports_recreation에서 데이터 조회
            sports_data = self.get_sports_recreation_data()
            if not sports_data:
                logger.warning("마이그레이션할 데이터가 없습니다.")
                return False
            
            # 2. spots 형식으로 변환
            spots_data = self.transform_to_spots_data(sports_data)
            logger.info(f"데이터 변환 완료: {len(spots_data)}건")
            
            # 3. spots 테이블에 삽입
            success = self.insert_into_spots(spots_data)
            
            if success:
                logger.info("✅ 레포츠 데이터 마이그레이션 완료!")
            else:
                logger.error("❌ 레포츠 데이터 마이그레이션 실패")
            
            return success
            
        except Exception as e:
            logger.error(f"마이그레이션 중 오류 발생: {str(e)}")
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
    
    # 마이그레이션 실행
    migrator = SportsRecreationMigrator(db_config)
    migrator.migrate_data()


if __name__ == "__main__":
    main()