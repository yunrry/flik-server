import mysql.connector
import requests
import json
import time
from typing import Dict, List, Optional
from dotenv import load_dotenv
import os

load_dotenv()

class DetailCommonDataUpdater:
    def __init__(self, service_key: str, db_config: Dict[str, str]):
        self.service_key = service_key
        self.db_config = db_config
        self.base_url = "http://apis.data.go.kr/B551011/KorService2/detailCommon2"
        
        # 테이블 매핑
        self.table_mapping = {
            "12": "fetched_tourist_attractions",
            "14": "fetched_cultural_facilities", 
            "15": "fetched_festivals_events",
            "28": "fetched_sports_recreation",
            "32": "fetched_accommodations",
            "38": "fetched_shopping",
            "39": "fetched_restaurants"
        }
        
        # JSON 파일의 분류 매핑
        self.classification_mapping = self._load_classification_mapping()
    
    def _load_classification_mapping(self) -> Dict[str, Dict[str, str]]:
        """JSON 파일에서 분류 매핑 로드"""
        mapping = {}
        
        # JSON 파일 경로 (실제 경로로 수정 필요)
        json_file_path = "response_1757668259132.json"
        
        try:
            with open(json_file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                
            for item in data['response']['body']['items']['item']:
                lcls1_cd = item['lclsSystm1Cd']
                lcls2_cd = item['lclsSystm2Cd'] 
                lcls3_cd = item['lclsSystm3Cd']
                
                mapping[lcls3_cd] = {
                    'label_depth1': item['lclsSystm1Nm'],
                    'label_depth2': item['lclsSystm2Nm'],
                    'label_depth3': item['lclsSystm3Nm']
                }
                
        except Exception as e:
            print(f"JSON 파일 로드 실패: {e}")
            
        return mapping
    
    def add_columns_to_tables(self):
        """모든 fetched_ 테이블에 컬럼 추가"""
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor()
        
        try:
            for table_name in self.table_mapping.values():
                # 컬럼 추가 쿼리
                alter_queries = [
                    f"ALTER TABLE {table_name} ADD COLUMN label_depth1 VARCHAR(100) DEFAULT NULL",
                    f"ALTER TABLE {table_name} ADD COLUMN label_depth2 VARCHAR(100) DEFAULT NULL", 
                    f"ALTER TABLE {table_name} ADD COLUMN label_depth3 VARCHAR(100) DEFAULT NULL",
                    f"ALTER TABLE {table_name} ADD COLUMN overview TEXT DEFAULT NULL"
                ]
                
                for query in alter_queries:
                    try:
                        cursor.execute(query)
                        print(f"✓ {table_name}에 컬럼 추가 성공")
                    except mysql.connector.Error as e:
                        if e.errno == 1060:  # Duplicate column name
                            print(f"⚠ {table_name}에 컬럼이 이미 존재함")
                        else:
                            print(f"✗ {table_name} 컬럼 추가 실패: {e}")
                            
            conn.commit()
            print("모든 테이블에 컬럼 추가 완료")
            
        except Exception as e:
            print(f"컬럼 추가 중 오류: {e}")
            conn.rollback()
        finally:
            cursor.close()
            conn.close()
    
    def fetch_detail_common(self, content_id: str) -> Optional[Dict]:
        """detailCommon2 API 호출"""
        params = {
            'MobileOS': 'WEB',
            'MobileApp': 'Flik',
            '_type': 'json',
            'contentId': content_id,
            'serviceKey': self.service_key
        }
        
        try:
            response = requests.get(self.base_url, params=params, timeout=30)
            response.raise_for_status()
            
            data = response.json()
            
            if data['response']['header']['resultCode'] == '0000':
                items = data['response']['body']['items']
                if 'item' in items and items['item']:
                    return items['item'][0]
            
            return None
            
        except Exception as e:
            print(f"API 호출 실패 (contentId: {content_id}): {e}")
            return None
    
    def get_content_ids_by_type(self, content_type: str) -> List[str]:
        """특정 content_type의 content_id 목록 조회"""
        table_name = self.table_mapping[content_type]
        
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor()
        
        try:
            query = f"SELECT content_id FROM {table_name} WHERE content_id IS NOT NULL"
            cursor.execute(query)
            results = cursor.fetchall()
            return [row[0] for row in results]
        except Exception as e:
            print(f"content_id 조회 실패: {e}")
            return []
        finally:
            cursor.close()
            conn.close()
    
    def update_table_data(self, content_type: str, content_id: str, 
                         label_depth1: str, label_depth2: str, label_depth3: str, 
                         overview: str):
        """테이블 데이터 업데이트"""
        table_name = self.table_mapping[content_type]
        
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor()
        
        try:
            update_query = f"""
                UPDATE {table_name} 
                SET label_depth1 = %s, label_depth2 = %s, label_depth3 = %s, overview = %s
                WHERE content_id = %s
            """
            
            cursor.execute(update_query, (label_depth1, label_depth2, label_depth3, overview, content_id))
            conn.commit()
            
            if cursor.rowcount > 0:
                print(f"✓ {table_name} 업데이트 성공: {content_id}")
            else:
                print(f"⚠ {table_name} 업데이트된 행 없음: {content_id}")
                
        except Exception as e:
            print(f"✗ {table_name} 업데이트 실패: {e}")
            conn.rollback()
        finally:
            cursor.close()
            conn.close()
    
    def process_content_type(self, content_type: str):
        """특정 content_type 처리"""
        print(f"\n=== {content_type} 처리 시작 ===")
        
        content_ids = self.get_content_ids_by_type(content_type)
        print(f"처리할 content_id 개수: {len(content_ids)}")
        
        success_count = 0
        error_count = 0
        
        for i, content_id in enumerate(content_ids, 1):
            print(f"[{i}/{len(content_ids)}] 처리 중: {content_id}")
            
            # API 호출
            detail_data = self.fetch_detail_common(content_id)
            
            if detail_data:
                # 분류 정보 추출
                lcls3_cd = detail_data.get('lclsSystm3', '')
                classification = self.classification_mapping.get(lcls3_cd, {})
                
                label_depth1 = classification.get('label_depth1', '')
                label_depth2 = classification.get('label_depth2', '')
                label_depth3 = classification.get('label_depth3', '')
                overview = detail_data.get('overview', '')
                
                # 데이터베이스 업데이트
                self.update_table_data(
                    content_type, content_id,
                    label_depth1, label_depth2, label_depth3, overview
                )
                
                success_count += 1
            else:
                error_count += 1
                print(f"✗ API 응답 없음: {content_id}")
            
            # API 호출 제한 고려
            time.sleep(0.1)
        
        print(f"\n=== {content_type} 처리 완료 ===")
        print(f"성공: {success_count}, 실패: {error_count}")
    
    def process_all_content_types(self):
        """모든 content_type 처리"""
        print("=== 모든 fetched_ 테이블 데이터 업데이트 시작 ===")
        
        # 1. 컬럼 추가
        print("\n1. 컬럼 추가 중...")
        self.add_columns_to_tables()
        
        # 2. 각 content_type별 처리
        for content_type in self.table_mapping.keys():
            self.process_content_type(content_type)
        
        print("\n=== 모든 처리 완료 ===")

def main():
    # 환경변수 로드
    service_key = os.getenv('TOUR_API_KEY')
    if not service_key:
        print("TOUR_API_KEY 환경변수가 설정되지 않았습니다.")
        return
    
    db_config = {
        'host': os.getenv('MYSQL_HOST', 'localhost'),
        'user': os.getenv('MYSQL_USER', 'root'),
        'password': os.getenv('MYSQL_PASSWORD', ''),
        'database': os.getenv('MYSQL_NAME', 'flik_db'),
        'charset': 'utf8mb4'
    }
    
    # 업데이터 실행
    updater = DetailCommonDataUpdater(service_key, db_config)
    updater.process_all_content_types()

if __name__ == "__main__":
    main()