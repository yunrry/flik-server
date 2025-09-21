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

class TouristAttractionsMigrator:
    def __init__(self, db_config: Dict[str, str]):
        self.db_config = db_config
        
        # 전국 지역 코드 매핑 (area_code -> regn_cd)
        self.region_mapping = {
            '1': '11',   # 서울시
            '2': '28',   # 인천시
            '3': '30',   # 대전시
            '4': '27',   # 대구시
            '5': '29',   # 광주시
            '6': '26',   # 부산시
            '7': '31',   # 울산시
            '9': '36',   # 경기도
            '10': '41',  # 강원도
            '11': '43',  # 충청북도
            '12': '44',  # 충청남도
            '13': '48',  # 경상북도
            '14': '50',  # 경상남도
            '15': '46',  # 전라북도
            '16': '47',  # 전라남도
            '17': '51'   # 제주도
        }
        
        # 전국 시군구 매핑 (지역별 시군구명 -> 코드)
        self.sigungu_mapping = {
            # 서울시
            '종로구': '110', '중구': '140', '용산구': '170', '성동구': '200', '광진구': '215',
            '동대문구': '230', '중랑구': '260', '성북구': '290', '강북구': '305', '도봉구': '320',
            '노원구': '350', '은평구': '380', '서대문구': '410', '마포구': '440', '양천구': '470',
            '강서구': '500', '구로구': '530', '금천구': '545', '영등포구': '560', '동작구': '590',
            '관악구': '620', '서초구': '650', '강남구': '680', '송파구': '710', '강동구': '740',
            
            # 부산시
            '중구': '110', '서구': '140', '동구': '170', '영도구': '200', '부산진구': '230',
            '동래구': '260', '남구': '290', '북구': '320', '해운대구': '350', '사하구': '380',
            '금정구': '410', '강서구': '440', '연제구': '470', '수영구': '500', '사상구': '530',
            '기장군': '710',
            
            # 대구시
            '중구': '110', '동구': '140', '서구': '170', '남구': '200', '북구': '230',
            '수성구': '260', '달서구': '290', '달성군': '710', '군위군': '720',
            
            # 인천시
            '중구': '110', '동구': '140', '미추홀구': '177', '연수구': '185', '남동구': '200',
            '부평구': '237', '계양구': '245', '서구': '260', '강화군': '710', '옹진군': '720',
            
            # 광주시
            '동구': '110', '서구': '140', '남구': '155', '북구': '170', '광산구': '200',
            
            # 대전시
            '동구': '110', '중구': '140', '서구': '170', '유성구': '200', '대덕구': '230',
            
            # 울산시
            '중구': '110', '남구': '140', '동구': '170', '북구': '200', '울주군': '710',
            
            # 세종시
            '세종특별자치시': '110',
            
            # 경기도
            '수원시': '110', '성남시': '130', '의정부시': '150', '안양시': '170', '부천시': '190',
            '광명시': '210', '평택시': '220', '동두천시': '250', '안산시': '270', '고양시': '280',
            '과천시': '290', '구리시': '310', '남양주시': '360', '오산시': '370', '시흥시': '390',
            '군포시': '410', '의왕시': '430', '하남시': '450', '용인시': '460', '파주시': '480',
            '이천시': '500', '안성시': '550', '김포시': '570', '화성시': '590', '광주시': '610',
            '양주시': '630', '포천시': '650', '여주시': '670', '연천군': '800', '가평군': '820',
            '양평군': '830',
            
            # 강원도
            '춘천시': '110', '원주시': '130', '강릉시': '150', '동해시': '170', '태백시': '190',
            '속초시': '210', '삼척시': '230', '홍천군': '720', '횡성군': '730', '영월군': '750',
            '평창군': '760', '정선군': '770', '철원군': '780', '화천군': '790', '양구군': '800',
            '인제군': '810', '고성군': '820', '양양군': '830',
            
            # 충청북도
            '청주시': '110', '충주시': '130', '제천시': '150', '보은군': '720', '옥천군': '730',
            '영동군': '740', '증평군': '745', '진천군': '750', '괴산군': '760', '음성군': '770',
            '단양군': '800',
            
            # 충청남도
            '천안시': '110', '공주시': '150', '보령시': '180', '아산시': '200', '서산시': '210',
            '논산시': '230', '계룡시': '250', '당진시': '270', '금산군': '710', '부여군': '760',
            '서천군': '770', '청양군': '790', '홍성군': '800', '예산군': '810', '태안군': '825',
            
            # 전라북도
            '전주시': '110', '군산시': '130', '익산시': '140', '정읍시': '180', '남원시': '190',
            '김제시': '210', '완주군': '710', '진안군': '720', '무주군': '730', '장수군': '740',
            '임실군': '750', '순창군': '770', '고창군': '790', '부안군': '800',
            
            # 전라남도
            '목포시': '110', '여수시': '130', '순천시': '150', '나주시': '170', '광양시': '230',
            '담양군': '710', '곡성군': '720', '구례군': '730', '고흥군': '770', '보성군': '780',
            '화순군': '790', '장흥군': '800', '강진군': '810', '해남군': '820', '영암군': '830',
            '무안군': '840', '함평군': '860', '영광군': '870', '장성군': '880', '완도군': '890',
            '진도군': '900', '신안군': '910',
            
            # 경상북도
            '포항시': '110', '경주시': '130', '김천시': '150', '안동시': '170', '구미시': '190',
            '영주시': '210', '영천시': '230', '상주시': '250', '문경시': '280', '경산시': '290',
            '의성군': '730', '청송군': '750', '영양군': '760', '영덕군': '770', '청도군': '820',
            '고령군': '830', '성주군': '840', '칠곡군': '850', '예천군': '900', '봉화군': '920',
            '울진군': '930', '울릉군': '940',
            
            # 경상남도
            '진주시': '170', '통영시': '220', '사천시': '240', '김해시': '250', '밀양시': '270',
            '거제시': '310', '양산시': '330', '의령군': '720', '함안군': '730', '창녕군': '740',
            '고성군': '820', '남해군': '840', '하동군': '850', '산청군': '860', '함양군': '870',
            '거창군': '880',
            
            # 제주도
            '제주시': '110', '서귀포시': '130'
        }
    
    def parse_address_to_district(self, addr1: str) -> str:
        """주소에서 구 정보 추출하여 코드 반환"""
        if not addr1:
            return "110"  # 기본값: 종로구
        
        # 주소를 공백으로 분리하여 시군구명 추출 (두 번째 요소)
        addr_parts = addr1.strip().split()
        if len(addr_parts) < 2:
            return "110"  # 기본값
        
        # 시군구명 추출 (두 번째 요소)
        sigungu_name = addr_parts[1]
        
        # 매핑에서 찾기
        if sigungu_name in self.sigungu_mapping:
            return self.sigungu_mapping[sigungu_name]
        
        # 매칭되지 않으면 기본값
        return "110"
    
    def get_region_code(self, area_code: str) -> str:
        """area_code로 지역코드 반환"""
        if not area_code or area_code.strip() == '':
            logger.warning("area_code가 비어있음, 기본값(서울시) 사용")
            return '11'

        # 공백 제거 및 문자열 정리
        area_code_clean = str(area_code).strip()

        # 매핑에서 찾기
        region_code = self.region_mapping.get(area_code_clean, '11')

        if region_code == '11' and area_code_clean != '1':
            logger.warning(f"area_code '{area_code_clean}'에 대한 매핑을 찾을 수 없음, 기본값(서울시) 사용")

        return region_code


    def get_tourist_attractions_data(self) -> List[Dict[str, Any]]:
        """fetched_tourist_attractions 테이블에서 데이터 조회"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor(dictionary=True)

            # label_depth1이 존재하는 데이터만 조회
            query = """
            SELECT * FROM fetched_tourist_attractions 
            WHERE label_depth1 IS NOT NULL 
            AND label_depth1 != '' 
            ORDER BY id
            """
            cursor.execute(query)
            data = cursor.fetchall()

            logger.info(f"fetched_tourist_attractions에서 {len(data)}건 조회")
            return data

        except Error as e:
            logger.error(f"데이터 조회 실패: {e}")
            return []
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
        

    def transform_to_spots_data(self, tourist_data: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """fetched_tourist_attractions 데이터를 spots 형식으로 변환"""
        spots_data = []

        for item in tourist_data:
            # area_code로 지역코드 추출
            regn_cd = self.get_region_code(item.get('area_code', '1'))
            
            # 주소에서 시군구 코드 추출
            signgu_cd = self.parse_address_to_district(item.get('addr1', ''))


            # 이미지 URL 처리
            image_urls = []
            if item.get('first_image'):
                image_urls.append(item['first_image'])
            if item.get('first_image2'):
                image_urls.append(item['first_image2'])

            spot_data = {
                'spot_type': 'TOUR_SPOT',
                'address': item.get('addr1', ''),
                'content_id': item.get('content_id', ''),
                'baby_carriage': item.get('chkbabycarriage', ''),
                'category': item.get('content_type_name', ''),
                'close_time': None,  # None으로 설정
                'content_type_id': item.get('content_type_id', ''),
                'day_off': item.get('restdate', ''),
                'description': item.get('overview', ''),
                'google_place_id': item.get('google_place_id', ''),
                'image_urls': json.dumps(image_urls, ensure_ascii=False) if image_urls else None,
                'info': item.get('infocenter', ''),
                'latitude': float(item.get('map_y', 0)) if item.get('map_y') else None,
                'longitude': float(item.get('map_x', 0)) if item.get('map_x') else None,
                'name': item.get('title', ''),
                'open_time': None,  # None으로 설정
                'parking': item.get('parking', ''),
                'pet_carriage': item.get('chkpet', ''),
                'rating': item.get('google_rating', ''),
                'regn_cd': regn_cd,  # area_code로 추출한 지역코드
                'review_count': item.get('google_review_count', ''),
                'signgu_cd': signgu_cd,  # 주소에서 추출한 시군구코드
                'tag1': item.get('tag1', ''),
                'tag2': item.get('tag2', ''),
                'tag3': item.get('tag3', ''),
                'tags': item.get('tags', ''),
                'label_depth1': item.get('label_depth1', ''),
                'label_depth2': item.get('label_depth2', ''),
                'label_depth3': item.get('label_depth3', ''),
                'time': item.get('usetime', ''),  # 새로 추가한 time 컬럼에 usetime 값 매핑
                'exp_guide': item.get('expguide', ''),
                'google_reviews': item.get('google_reviews', '')
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
                review_count, signgu_cd, tag1, tag2, tag3, tags, label_depth1, label_depth2, label_depth3, time, exp_guide, google_reviews
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
                    item.get('close_time'),  # None
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
                    item.get('open_time'),  # None
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
                    item.get('time', ''),  # usetime 값
                    item.get('exp_guide', ''),
                    item.get('google_reviews', '')
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
        logger.info("관광지 데이터 마이그레이션 시작")
        
        try:
            # 1. fetched_tourist_attractions에서 데이터 조회
            tourist_data = self.get_tourist_attractions_data()
            if not tourist_data:
                logger.warning("마이그레이션할 데이터가 없습니다.")
                return False
            
            # 2. spots 형식으로 변환
            spots_data = self.transform_to_spots_data(tourist_data)
            logger.info(f"데이터 변환 완료: {len(spots_data)}건")
            
            # 3. spots 테이블에 삽입
            success = self.insert_into_spots(spots_data)
            
            if success:
                logger.info("✅ 관광지 데이터 마이그레이션 완료!")
            else:
                logger.error("❌ 관광지 데이터 마이그레이션 실패")
            
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
    migrator = TouristAttractionsMigrator(db_config)
    migrator.migrate_data()


if __name__ == "__main__":
    main()