import asyncio
import aiohttp
import pandas as pd
import json
from datetime import datetime
import logging
from typing import List, Dict, Any
import time
import os
import mysql.connector
from mysql.connector import Error
from dotenv import load_dotenv

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class TourismDataCollector:
    def __init__(self, service_key: str, db_config: Dict[str, str]):
        self.service_key = service_key
        self.db_config = db_config
        self.base_url = "http://apis.data.go.kr/B551011/KorService2/areaBasedList2"
        self.seoul_area_code = "1"  # 서울특별시
        self.content_types = {
            "12": "관광지",
            "14": "문화시설", 
            "15": "축제공연행사",
            "25": "여행코스",
            "28": "레포츠",
            "32": "숙박",
            "38": "쇼핑",
            "39": "음식점"
        }
        self.collected_data = []
        
    async def fetch_data(self, session: aiohttp.ClientSession, content_type: str, page_no: int) -> Dict[str, Any]:
        """API 호출하여 데이터 가져오기"""
        params = {
            "serviceKey": self.service_key,
            "numOfRows": 50,  # 페이지당 50개
            "pageNo": page_no,
            "MobileOS": "WEB",
            "MobileApp": "Flik",
            "_type": "json",
            "arrange": "C",  # 수정일순 (최신순)
            "contentTypeId": content_type,
            "areaCode": self.seoul_area_code
        }
        
        try:
            async with session.get(self.base_url, params=params) as response:
                if response.status == 200:
                    data = await response.json()
                    return {
                        "content_type": content_type,
                        "page": page_no,
                        "data": data,
                        "success": True
                    }
                else:
                    logger.error(f"API 호출 실패 - Content Type: {content_type}, Page: {page_no}, Status: {response.status}")
                    return {"content_type": content_type, "page": page_no, "success": False}
                    
        except Exception as e:
            logger.error(f"API 호출 중 오류 - Content Type: {content_type}, Page: {page_no}, Error: {str(e)}")
            return {"content_type": content_type, "page": page_no, "success": False}
    
    def parse_item_data(self, item: Dict[str, Any], content_type: str) -> Dict[str, Any]:
        """API 응답 아이템을 표준 형식으로 파싱"""
        # tel 필드 정제 (전화번호만 추출)
        tel = item.get("tel", "").strip()
        
        # 전화번호 형식 정리 (숫자, 하이픈, 괄호, 공백만 허용)
        import re
        if tel:
            # 전화번호에서 불필요한 문자 제거
            tel = re.sub(r'[^\d\-\(\)\s]', '', tel)
            # 연속된 공백을 하나로 변경
            tel = re.sub(r'\s+', ' ', tel).strip()
        
        return {
            "content_id": item.get("contentid", ""),
            "content_type_id": content_type,
            "content_type_name": self.content_types.get(content_type, ""),
            "title": item.get("title", ""),
            "addr1": item.get("addr1", ""),
            "addr2": item.get("addr2", ""),
            "first_image": item.get("firstimage", ""),
            "first_image2": item.get("firstimage2", ""),
            "map_x": item.get("mapx", ""),
            "map_y": item.get("mapy", ""),
            "area_code": item.get("areacode", ""),
            "sigungu_code": item.get("sigungucode", ""),
            "cat1": item.get("cat1", ""),
            "cat2": item.get("cat2", ""),
            "cat3": item.get("cat3", ""),
            "created_time": item.get("createdtime", ""),
            "modified_time": item.get("modifiedtime", ""),
            "tel": tel,  # 정제된 전화번호
            "zipcode": item.get("zipcode", ""),
            "overview": item.get("overview", ""),
            "raw_data": json.dumps(item, ensure_ascii=False)
        }
    
    async def collect_content_type_data(self, session: aiohttp.ClientSession, content_type: str, target_count: int) -> List[Dict[str, Any]]:
        """특정 컨텐츠 타입의 데이터 수집"""
        logger.info(f"{self.content_types[content_type]} 데이터 수집 시작 (목표: {target_count}건)")
        
        collected_items = []
        page = 1
        
        while len(collected_items) < target_count:
            result = await self.fetch_data(session, content_type, page)
            
            if not result["success"]:
                logger.warning(f"페이지 {page} 수집 실패")
                break
            
            response_data = result["data"]
            
            # API 응답 구조 확인
            if "response" not in response_data:
                logger.error("잘못된 API 응답 구조")
                break
                
            body = response_data["response"].get("body", {})
            items = body.get("items", {})
            
            if not items or "item" not in items:
                logger.info(f"더 이상 데이터가 없습니다. (페이지: {page})")
                break
            
            item_list = items["item"]
            if isinstance(item_list, dict):  # 단일 아이템인 경우
                item_list = [item_list]
            
            for item in item_list:
                if len(collected_items) >= target_count:
                    break
                    
                parsed_item = self.parse_item_data(item, content_type)
                collected_items.append(parsed_item)
            
            logger.info(f"{self.content_types[content_type]} - 페이지 {page} 완료 (수집: {len(item_list)}건, 총: {len(collected_items)}건)")
            
            # API 호출 제한을 위한 딜레이
            await asyncio.sleep(0.1)
            page += 1
            
            # 총 페이지 수 확인하여 무한 루프 방지
            total_count = body.get("totalCount", 0)
            if len(collected_items) >= total_count:
                break
        
        logger.info(f"{self.content_types[content_type]} 수집 완료: {len(collected_items)}건")
        return collected_items
    
    async def collect_all_data(self, total_target: int = 1000) -> List[Dict[str, Any]]:
        """모든 컨텐츠 타입 데이터 병렬 수집"""
        start_time = time.time()
        logger.info(f"서울 관광 데이터 수집 시작 (목표: {total_target}건)")
        
        # 컨텐츠 타입별 목표 건수 분배
        per_content_target = total_target // len(self.content_types)
        
        async with aiohttp.ClientSession() as session:
            # 병렬로 각 컨텐츠 타입 데이터 수집
            tasks = []
            for content_type in self.content_types.keys():
                task = self.collect_content_type_data(session, content_type, per_content_target)
                tasks.append(task)
            
            results = await asyncio.gather(*tasks, return_exceptions=True)
            
            # 결과 통합
            all_data = []
            for i, result in enumerate(results):
                if isinstance(result, Exception):
                    logger.error(f"컨텐츠 타입 {list(self.content_types.keys())[i]} 수집 실패: {str(result)}")
                else:
                    all_data.extend(result)
        
        end_time = time.time()
        logger.info(f"데이터 수집 완료 - 총 {len(all_data)}건, 소요시간: {end_time - start_time:.2f}초")
        
        return all_data
    
    def save_to_files(self, data: List[Dict[str, Any]], base_filename: str = "seoul_tourism_data"):
        """수집된 데이터를 파일로 저장"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        
        # DataFrame 생성
        df = pd.DataFrame(data)
        
        # CSV 저장
        csv_filename = f"{base_filename}_{timestamp}.csv"
        df.to_csv(csv_filename, index=False, encoding='utf-8-sig')
        logger.info(f"CSV 파일 저장: {csv_filename}")
        
        # JSON 저장 (원본 데이터 보존)
        json_filename = f"{base_filename}_{timestamp}.json"
        with open(json_filename, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        logger.info(f"JSON 파일 저장: {json_filename}")
        
        # 요약 정보 출력
        self.print_summary(df)
        
        return csv_filename, json_filename
    
    def print_summary(self, df: pd.DataFrame):
        """수집 데이터 요약 정보 출력"""
        logger.info("=== 수집 데이터 요약 ===")
        logger.info(f"총 데이터 건수: {len(df)}")
        
        # 컨텐츠 타입별 건수
        type_counts = df['content_type_name'].value_counts()
        for content_type, count in type_counts.items():
            logger.info(f"  - {content_type}: {count}건")
        
        # 이미지 있는 데이터 비율
        has_image = df['first_image'].notna() & (df['first_image'] != '')
        logger.info(f"이미지 포함 데이터: {has_image.sum()}건 ({has_image.mean()*100:.1f}%)")
        
        # 좌표 정보 있는 데이터 비율
        has_coords = df['map_x'].notna() & (df['map_x'] != '') & df['map_y'].notna() & (df['map_y'] != '')
        logger.info(f"좌표 정보 포함 데이터: {has_coords.sum()}건 ({has_coords.mean()*100:.1f}%)")
    
    def create_tables(self):
        """DB 테이블 생성"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            # 관광지 테이블 생성
            create_table_query = """
            CREATE TABLE IF NOT EXISTS tourism_spots (
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
                tel VARCHAR(100),
                zipcode VARCHAR(20),
                overview TEXT,
                raw_data JSON,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_content_type (content_type_id),
                INDEX idx_area (area_code, sigungu_code),
                INDEX idx_title (title(100))
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """
            
            cursor.execute(create_table_query)
            connection.commit()
            logger.info("DB 테이블 생성 완료")
            
        except Error as e:
            logger.error(f"DB 테이블 생성 실패: {e}")
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
        
        return True
    
    def save_to_database(self, data: List[Dict[str, Any]]) -> bool:
        """수집된 데이터를 MySQL DB에 저장"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            # 테이블 생성
            if not self.create_tables():
                return False
            
            # 데이터 삽입 쿼리
            insert_query = """
            INSERT INTO tourism_spots (
                content_id, content_type_id, content_type_name, title, addr1, addr2,
                first_image, first_image2, map_x, map_y, area_code, sigungu_code,
                cat1, cat2, cat3, created_time, modified_time, tel, zipcode, overview, raw_data
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
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
                    item.get('raw_data', '')
                ))
            
            # 배치 삽입 실행
            cursor.executemany(insert_query, insert_data)
            connection.commit()
            
            logger.info(f"DB 저장 완료: {len(data)}건")
            return True
            
        except Error as e:
            logger.error(f"DB 저장 실패: {e}")
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()


async def main():
    # .env 파일 로드
    load_dotenv()
    
    # API 서비스 키 설정
    SERVICE_KEY = os.getenv('TOUR_API_KEY')
    
    if not SERVICE_KEY:
        logger.error("TOUR_API_KEY 환경변수를 설정해주세요!")
        return
    
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
    
    # 데이터 수집기 초기화
    collector = TourismDataCollector(SERVICE_KEY, db_config)
    
    try:
        # 데이터 수집 (1000건)
        data = await collector.collect_all_data(1000)
        
        if data:
            # DB에 저장
            db_success = collector.save_to_database(data)
            
            if db_success:
                logger.info("데이터 수집 및 DB 저장 완료!")
                
                # 백업 파일도 저장
                backup_file = collector.save_to_files(data, "seoul_tourism_backup")
                logger.info(f"백업 파일: {backup_file}")
                
                # 요약 정보 출력
                collector.print_summary(pd.DataFrame(data))
            else:
                logger.error("DB 저장 실패 - 백업 파일만 저장")
                collector.save_to_files(data, "seoul_tourism_failed")
        else:
            logger.warning("수집된 데이터가 없습니다.")
            
    except Exception as e:
        logger.error(f"데이터 수집 중 오류 발생: {str(e)}")


if __name__ == "__main__":
    # 실행
    asyncio.run(main())