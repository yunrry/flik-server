import mysql.connector
import json
import os
from typing import Dict, List, Tuple
from dotenv import load_dotenv

load_dotenv()

class LabelTableManager:
    def __init__(self, db_config: Dict[str, str]):
        self.db_config = db_config
        self.json_file_path = "response_1757668259132.json"
    
    def create_label_table(self):
        """label 테이블 생성"""
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor()
        
        try:
            # 기존 테이블 삭제 (있다면)
            cursor.execute("DROP TABLE IF EXISTS label")
            
            # label 테이블 생성
            create_table_query = """
                CREATE TABLE label (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    code VARCHAR(20) NOT NULL UNIQUE,
                    name VARCHAR(100) NOT NULL,
                    depth INT NOT NULL,
                    parent_code VARCHAR(20) DEFAULT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_code (code),
                    INDEX idx_depth (depth),
                    INDEX idx_parent_code (parent_code)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """
            
            cursor.execute(create_table_query)
            conn.commit()
            print("✓ label 테이블 생성 완료")
            
        except Exception as e:
            print(f"✗ label 테이블 생성 실패: {e}")
            conn.rollback()
        finally:
            cursor.close()
            conn.close()
    
    def load_classification_data(self) -> List[Dict]:
        """JSON 파일에서 분류 데이터 로드"""
        try:
            with open(self.json_file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            items = data['response']['body']['items']['item']
            print(f"✓ JSON 파일 로드 완료: {len(items)}개 항목")
            return items
            
        except Exception as e:
            print(f"✗ JSON 파일 로드 실패: {e}")
            return []
    
    def parse_classification_data(self, items: List[Dict]) -> List[Tuple]:
        """분류 데이터를 파싱하여 (code, name, depth, parent_code) 튜플 리스트 생성"""
        parsed_data = []
        seen_codes = set()
        
        for item in items:
            lcls1_cd = item['lclsSystm1Cd']
            lcls1_nm = item['lclsSystm1Nm']
            lcls2_cd = item['lclsSystm2Cd']
            lcls2_nm = item['lclsSystm2Nm']
            lcls3_cd = item['lclsSystm3Cd']
            lcls3_nm = item['lclsSystm3Nm']
            
            # Depth 1 (대분류)
            if lcls1_cd not in seen_codes:
                parsed_data.append((lcls1_cd, lcls1_nm, 1, None))
                seen_codes.add(lcls1_cd)
            
            # Depth 2 (중분류)
            if lcls2_cd not in seen_codes:
                parsed_data.append((lcls2_cd, lcls2_nm, 2, lcls1_cd))
                seen_codes.add(lcls2_cd)
            
            # Depth 3 (소분류)
            if lcls3_cd not in seen_codes:
                parsed_data.append((lcls3_cd, lcls3_nm, 3, lcls2_cd))
                seen_codes.add(lcls3_cd)
        
        print(f"✓ 분류 데이터 파싱 완료: {len(parsed_data)}개 항목")
        return parsed_data
    
    def insert_label_data(self, parsed_data: List[Tuple]):
        """label 테이블에 데이터 삽입"""
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor()
        
        try:
            insert_query = """
                INSERT INTO label (code, name, depth, parent_code) 
                VALUES (%s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE 
                    name = VALUES(name),
                    depth = VALUES(depth),
                    parent_code = VALUES(parent_code)
            """
            
            cursor.executemany(insert_query, parsed_data)
            conn.commit()
            
            print(f"✓ label 데이터 삽입 완료: {cursor.rowcount}개 행")
            
        except Exception as e:
            print(f"✗ label 데이터 삽입 실패: {e}")
            conn.rollback()
        finally:
            cursor.close()
            conn.close()
    
    def add_label_columns_to_fetched_tables(self):
        """fetched_ 테이블들에 label_code 컬럼 추가"""
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor()
        
        table_names = [
            "fetched_tourist_attractions",
            "fetched_cultural_facilities", 
            "fetched_festivals_events",
            "fetched_sports_recreation",
            "fetched_accommodations",
            "fetched_shopping",
            "fetched_restaurants"
        ]
        
        try:
            for table_name in table_names:
                # label_code 컬럼 추가
                alter_query = f"""
                    ALTER TABLE {table_name} 
                    ADD COLUMN label_code VARCHAR(20) DEFAULT NULL,
                    ADD INDEX idx_label_code (label_code)
                """
                
                try:
                    cursor.execute(alter_query)
                    print(f"✓ {table_name}에 label_code 컬럼 추가 성공")
                except mysql.connector.Error as e:
                    if e.errno == 1060:  # Duplicate column name
                        print(f"⚠ {table_name}에 label_code 컬럼이 이미 존재함")
                    else:
                        print(f"✗ {table_name} label_code 컬럼 추가 실패: {e}")
            
            conn.commit()
            print("✓ 모든 fetched_ 테이블에 label_code 컬럼 추가 완료")
            
        except Exception as e:
            print(f"✗ 컬럼 추가 중 오류: {e}")
            conn.rollback()
        finally:
            cursor.close()
            conn.close()
    
    def update_fetched_tables_with_label_codes(self):
        """fetched_ 테이블들의 label_code 업데이트"""
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor()
        
        # content_type별 lclsSystm3 매핑
        content_type_mapping = {
            "12": "fetched_tourist_attractions",
            "14": "fetched_cultural_facilities",
            "15": "fetched_festivals_events", 
            "28": "fetched_sports_recreation",
            "32": "fetched_accommodations",
            "38": "fetched_shopping",
            "39": "fetched_restaurants"
        }
        
        try:
            for content_type, table_name in content_type_mapping.items():
                # 해당 테이블의 lclsSystm3 값들을 label_code로 업데이트
                update_query = f"""
                    UPDATE {table_name} 
                    SET label_code = lclsSystm3 
                    WHERE lclsSystm3 IS NOT NULL AND lclsSystm3 != ''
                """
                
                cursor.execute(update_query)
                updated_rows = cursor.rowcount
                print(f"✓ {table_name} label_code 업데이트: {updated_rows}개 행")
            
            conn.commit()
            print("✓ 모든 fetched_ 테이블 label_code 업데이트 완료")
            
        except Exception as e:
            print(f"✗ label_code 업데이트 실패: {e}")
            conn.rollback()
        finally:
            cursor.close()
            conn.close()
    
    def create_label_mapping_view(self):
        """label 매핑을 위한 뷰 생성"""
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor()
        
        try:
            # 각 fetched_ 테이블별로 label과 조인하는 뷰 생성
            table_names = [
                "fetched_tourist_attractions",
                "fetched_cultural_facilities", 
                "fetched_festivals_events",
                "fetched_sports_recreation",
                "fetched_accommodations",
                "fetched_shopping",
                "fetched_restaurants"
            ]
            
            for table_name in table_names:
                view_name = f"v_{table_name}_with_labels"
                
                create_view_query = f"""
                    CREATE OR REPLACE VIEW {view_name} AS
                    SELECT 
                        f.*,
                        l1.name as label_depth1_name,
                        l2.name as label_depth2_name,
                        l3.name as label_depth3_name
                    FROM {table_name} f
                    LEFT JOIN label l3 ON f.label_code = l3.code AND l3.depth = 3
                    LEFT JOIN label l2 ON l3.parent_code = l2.code AND l2.depth = 2
                    LEFT JOIN label l1 ON l2.parent_code = l1.code AND l1.depth = 1
                """
                
                cursor.execute(create_view_query)
                print(f"✓ {view_name} 뷰 생성 완료")
            
            conn.commit()
            print("✓ 모든 label 매핑 뷰 생성 완료")
            
        except Exception as e:
            print(f"✗ 뷰 생성 실패: {e}")
            conn.rollback()
        finally:
            cursor.close()
            conn.close()
    
    def run_full_setup(self):
        """전체 설정 실행"""
        print("=== Label 테이블 및 매핑 설정 시작 ===")
        
        # 1. JSON 데이터 로드
        print("\n1. JSON 데이터 로드 중...")
        items = self.load_classification_data()
        if not items:
            print("✗ JSON 데이터 로드 실패로 종료")
            return
        
        # 2. 분류 데이터 파싱
        print("\n2. 분류 데이터 파싱 중...")
        parsed_data = self.parse_classification_data(items)
        
        # 3. label 테이블 생성
        print("\n3. label 테이블 생성 중...")
        self.create_label_table()
        
        # 4. label 데이터 삽입
        print("\n4. label 데이터 삽입 중...")
        self.insert_label_data(parsed_data)
        
        # 5. fetched_ 테이블에 label_code 컬럼 추가
        print("\n5. fetched_ 테이블에 label_code 컬럼 추가 중...")
        self.add_label_columns_to_fetched_tables()
        
        # 6. fetched_ 테이블의 label_code 업데이트
        print("\n6. fetched_ 테이블 label_code 업데이트 중...")
        self.update_fetched_tables_with_label_codes()
        
        # 7. label 매핑 뷰 생성
        print("\n7. label 매핑 뷰 생성 중...")
        self.create_label_mapping_view()
        
        print("\n=== Label 테이블 및 매핑 설정 완료 ===")

def main():
    # 데이터베이스 설정
    db_config = {
        'host': os.getenv('MYSQL_HOST', 'localhost'),
        'user': os.getenv('SPRING_DATASOURCE_USERNAME', 'root'),
        'password': os.getenv('SPRING_DATASOURCE_PASSWORD', ''),
        'database': os.getenv('MYSQL_NAME', 'flik_db'),
        'charset': 'utf8mb4'
    }
    
    # Label 테이블 매니저 실행
    manager = LabelTableManager(db_config)
    manager.run_full_setup()

if __name__ == "__main__":
    main()