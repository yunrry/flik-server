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

class FestivalsEventsMigrator:
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
        """Google 리뷰에서 축제/이벤트 관련 키워드 추출"""
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
            
            # 축제/이벤트 관련 키워드 추출
            event_keywords = re.findall(r'[가-힣]{2,4}(?:축제|행사|이벤트|공연|전시|콘서트|뮤지컬|연극|전시회|박람회|페스티벌|문화|예술|음악|무용|미술|문학)', all_text)
            mood_keywords = re.findall(r'[가-힣]{2,4}(?:즐거|재미있|흥미로|신나|감동|아름다|멋있|화려|웅장|소중|특별|기억|추억|볼거리|체험|참여)', all_text)
            atmosphere_keywords = re.findall(r'[가-힣]{2,4}(?:분위기|환경|시설|무대|장소|회장|야외|실내|조명|음향|정원|공원|광장|거리)', all_text)
            
            # 모든 키워드 합치기
            all_keywords = event_keywords + mood_keywords + atmosphere_keywords
            
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
    
    def get_festivals_events_data(self) -> List[Dict[str, Any]]:
        """fetched_festivals_events 테이블에서 데이터 조회"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor(dictionary=True)
            
            query = "SELECT * FROM fetched_festivals_events ORDER BY id"
            cursor.execute(query)
            data = cursor.fetchall()
            
            logger.info(f"fetched_festivals_events에서 {len(data)}건 조회")
            return data
            
        except Error as e:
            logger.error(f"데이터 조회 실패: {e}")
            return []
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def transform_to_spots_data(self, festivals_data: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """fetched_festivals_events 데이터를 spots 형식으로 변환"""
        spots_data = []
        
        for item in festivals_data:
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
                'spot_type': 'FESTIVAL',
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
                'rating': None,
                'regn_cd': '11',  # 서울특별시
                'review_count': None,
                'signgu_cd': signgu_cd,
                'tag1': keywords[0] if len(keywords) > 0 else '',
                'tag2': keywords[1] if len(keywords) > 1 else '',
                'tag3': keywords[2] if len(keywords) > 2 else '',
                'tags': ','.join(keywords) if keywords else '',
                'check_in_time': '',  # 축제/이벤트에는 해당없음
                'check_out_time': '',  # 축제/이벤트에는 해당없음
                'cooking': None,  # 축제/이벤트에는 해당없음 (bit 타입)
                'facilities': '',  # 축제/이벤트에는 해당없음
                'cuisine_type': '',  # 축제/이벤트에는 해당없음
                'fee': item.get('discountinfofestival', ''),  # 할인정보 -> 요금정보
                'age_limit': item.get('agelimit', ''),  # 연령제한
                'event_end_date': item.get('eventenddate', ''),  # 이벤트 종료일
                'event_start_date': item.get('eventstartdate', ''),  # 이벤트 시작일
                'running_time': item.get('playtime', ''),  # 공연시간
                'sponsor': item.get('sponsor1', ''),  # 주최자
                'first_menu': '',  # 축제/이벤트에는 해당없음
                'kids_facility': '',  # 축제/이벤트에는 해당없음
                'price_range': item.get('festivalgrade', ''),  # 축제등급
                'reservation': item.get('bookingplace', ''),  # 예약처
                'take_away': '',  # 축제/이벤트에는 해당없음
                'treat_menu': '',  # 축제/이벤트에는 해당없음
                'products': item.get('program', ''),  # 프로그램 정보
                'exp_guide': item.get('placeinfo', ''),  # 장소정보
                'time': item.get('usetime', '')  # 이용시간
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
            
            # spots 테이블의 정확한 컬럼 순서 (DDL 기준)
            insert_query = """
            INSERT INTO spots (
                spot_type, address, baby_carriage, category, close_time, content_type_id, content_id,
                day_off, description, google_place_id, image_urls, info, latitude,
                longitude, name, open_time, parking, pet_carriage, rating, regn_cd,
                review_count, signgu_cd, tag1, tag2, tag3, tags, check_in_time,
                check_out_time, cooking, facilities, cuisine_type, fee, age_limit,
                event_end_date, event_start_date, running_time, sponsor, first_menu,
                kids_facility, price_range, reservation, take_away, treat_menu,
                products, exp_guide, time
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
            )
            """
            
            # 배치 삽입을 위한 데이터 준비
            insert_data = []
            for i, item in enumerate(spots_data):
                try:
                    # spots 테이블의 모든 컬럼에 대응하는 데이터 (45개)
                    row_data = (
                        item.get('spot_type', ''),           # spot_type
                        item.get('address', ''),             # address
                        item.get('baby_carriage', ''),       # baby_carriage
                        item.get('category', ''),            # category
                        item.get('close_time'),              # close_time
                        item.get('content_type_id', ''),     # content_type_id
                        item.get('content_id', ''),          # content_id
                        item.get('day_off', ''),             # day_off
                        item.get('description', ''),         # description
                        item.get('google_place_id', ''),     # google_place_id
                        item.get('image_urls'),              # image_urls
                        item.get('info', ''),                # info
                        item.get('latitude'),                # latitude
                        item.get('longitude'),               # longitude
                        item.get('name', ''),                # name
                        item.get('open_time'),               # open_time
                        item.get('parking', ''),             # parking
                        item.get('pet_carriage', ''),        # pet_carriage
                        item.get('rating'),                  # rating
                        item.get('regn_cd', ''),             # regn_cd
                        item.get('review_count'),            # review_count
                        item.get('signgu_cd', ''),           # signgu_cd
                        item.get('tag1', ''),                # tag1
                        item.get('tag2', ''),                # tag2
                        item.get('tag3', ''),                # tag3
                        item.get('tags', ''),                # tags
                        item.get('check_in_time', ''),       # check_in_time
                        item.get('check_out_time', ''),      # check_out_time
                        item.get('cooking'),                 # cooking (bit)
                        item.get('facilities', ''),          # facilities
                        item.get('cuisine_type', ''),        # cuisine_type
                        item.get('fee', ''),                 # fee
                        item.get('age_limit', ''),           # age_limit
                        item.get('event_end_date', ''),      # event_end_date
                        item.get('event_start_date', ''),    # event_start_date
                        item.get('running_time', ''),        # running_time
                        item.get('sponsor', ''),             # sponsor
                        item.get('first_menu', ''),          # first_menu
                        item.get('kids_facility', ''),       # kids_facility
                        item.get('price_range', ''),         # price_range
                        item.get('reservation', ''),         # reservation
                        item.get('take_away', ''),           # take_away
                        item.get('treat_menu', ''),          # treat_menu
                        item.get('products', ''),            # products
                        item.get('exp_guide', ''),           # exp_guide
                        item.get('time', '')                 # time
                    )
                    
                    # 파라미터 개수 검증
                    if len(row_data) != 45:
                        logger.error(f"행 {i}: 파라미터 개수 불일치 - 예상: 45, 실제: {len(row_data)}")
                        continue
                    
                    insert_data.append(row_data)
                    
                except Exception as e:
                    logger.error(f"행 {i} 데이터 준비 실패: {e}")
                    continue
            
            if not insert_data:
                logger.error("삽입할 유효한 데이터가 없습니다.")
                return False
            
            # 배치 삽입 실행
            cursor.executemany(insert_query, insert_data)
            connection.commit()
            
            logger.info(f"spots 테이블에 {len(insert_data)}건 삽입 완료")
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
        logger.info("축제/이벤트 데이터 마이그레이션 시작")
        
        try:
            # 1. fetched_festivals_events에서 데이터 조회
            festivals_data = self.get_festivals_events_data()
            if not festivals_data:
                logger.warning("마이그레이션할 데이터가 없습니다.")
                return False
            
            # 2. spots 형식으로 변환
            spots_data = self.transform_to_spots_data(festivals_data)
            logger.info(f"데이터 변환 완료: {len(spots_data)}건")
            
            # 3. spots 테이블에 삽입
            success = self.insert_into_spots(spots_data)
            
            if success:
                logger.info("✅ 축제/이벤트 데이터 마이그레이션 완료!")
            else:
                logger.error("❌ 축제/이벤트 데이터 마이그레이션 실패")
            
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
    migrator = FestivalsEventsMigrator(db_config)
    migrator.migrate_data()


if __name__ == "__main__":
    main()