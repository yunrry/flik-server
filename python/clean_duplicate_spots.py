import mysql.connector
from mysql.connector import Error
import os
from dotenv import load_dotenv
import logging
from typing import Dict, List, Any, Tuple

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class DuplicateSpotsCleaner:
    """spots 테이블의 중복 레코드 정리 클래스"""
    
    def __init__(self, db_config: Dict[str, str]):
        self.db_config = db_config
        self.cleaned_count = 0
        self.kept_count = 0
    
    def get_duplicate_groups(self) -> List[Tuple[str, List[Dict[str, Any]]]]:
        """content_id가 같은 중복 그룹 조회"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor(dictionary=True)
            
            # content_id가 같은 레코드들을 그룹별로 조회
            query = """
            SELECT content_id, COUNT(*) as count
            FROM spots 
            WHERE content_id IS NOT NULL 
            AND content_id != ''
            GROUP BY content_id
            HAVING COUNT(*) > 1
            ORDER BY content_id
            """
            cursor.execute(query)
            duplicate_groups = cursor.fetchall()
            
            logger.info(f"중복 content_id 그룹 {len(duplicate_groups)}개 발견")
            
            # 각 그룹의 상세 데이터 조회
            groups_with_data = []
            for group in duplicate_groups:
                content_id = group['content_id']
                
                detail_query = """
                SELECT id, content_id, name, time, label_depth2
                FROM spots 
                WHERE content_id = %s
                ORDER BY id
                """
                cursor.execute(detail_query, (content_id,))
                records = cursor.fetchall()
                
                groups_with_data.append((content_id, records))
            
            return groups_with_data
            
        except Error as e:
            logger.error(f"중복 그룹 조회 실패: {e}")
            return []
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def select_record_to_keep(self, records: List[Dict[str, Any]]) -> Dict[str, Any]:
        """유지할 레코드 선택 (우선순위 적용)"""
        if len(records) <= 1:
            return records[0] if records else None
        
        # 1순위: time이 있는 레코드
        records_with_time = [r for r in records if r.get('time') and r.get('time').strip()]
        if len(records_with_time) == 1:
            logger.debug(f"time 기준으로 선택: {records_with_time[0]['id']}")
            return records_with_time[0]
        elif len(records_with_time) > 1:
            # time이 여러 개면 그 중에서 label_depth2가 있는 것 선택
            records_with_both = [r for r in records_with_time 
                               if r.get('label_depth2') and r.get('label_depth2').strip()]
            if records_with_both:
                logger.debug(f"time + label_depth2 기준으로 선택: {records_with_both[0]['id']}")
                return records_with_both[0]
            else:
                # time은 있지만 label_depth2가 없으면 첫 번째 선택
                logger.debug(f"time만 기준으로 선택: {records_with_time[0]['id']}")
                return records_with_time[0]
        
        # 2순위: label_depth2가 있는 레코드
        records_with_label = [r for r in records if r.get('label_depth2') and r.get('label_depth2').strip()]
        if len(records_with_label) == 1:
            logger.debug(f"label_depth2 기준으로 선택: {records_with_label[0]['id']}")
            return records_with_label[0]
        elif len(records_with_label) > 1:
            # label_depth2가 여러 개면 첫 번째 선택
            logger.debug(f"label_depth2 여러 개 중 첫 번째 선택: {records_with_label[0]['id']}")
            return records_with_label[0]
        
        # 3순위: 둘 다 없으면 첫 번째 레코드 선택
        logger.debug(f"기본 기준으로 선택: {records[0]['id']}")
        return records[0]
    
    def delete_duplicate_records(self, content_id: str, records: List[Dict[str, Any]], 
                               keep_record: Dict[str, Any]) -> int:
        """중복 레코드 삭제 (유지할 레코드 제외)"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            # 삭제할 레코드 ID 목록
            delete_ids = [r['id'] for r in records if r['id'] != keep_record['id']]
            
            if not delete_ids:
                return 0
            
            # 삭제 쿼리 실행
            placeholders = ','.join(['%s'] * len(delete_ids))
            delete_query = f"DELETE FROM spots WHERE id IN ({placeholders})"
            
            cursor.execute(delete_query, delete_ids)
            connection.commit()
            
            deleted_count = cursor.rowcount
            logger.info(f"content_id {content_id}: {deleted_count}건 삭제, {keep_record['id']} 유지")
            
            return deleted_count
            
        except Error as e:
            logger.error(f"content_id {content_id} 삭제 실패: {e}")
            return 0
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def clean_duplicates(self) -> bool:
        """중복 레코드 정리 실행"""
        logger.info("spots 테이블 중복 레코드 정리 시작")
        
        try:
            # 1. 중복 그룹 조회
            duplicate_groups = self.get_duplicate_groups()
            if not duplicate_groups:
                logger.info("정리할 중복 레코드가 없습니다.")
                return True
            
            # 2. 각 그룹별로 정리
            for content_id, records in duplicate_groups:
                logger.info(f"content_id {content_id}: {len(records)}개 레코드 처리 중...")
                
                # 유지할 레코드 선택
                keep_record = self.select_record_to_keep(records)
                if not keep_record:
                    logger.warning(f"content_id {content_id}: 유지할 레코드를 찾을 수 없습니다.")
                    continue
                
                # 중복 레코드 삭제
                deleted_count = self.delete_duplicate_records(content_id, records, keep_record)
                
                self.cleaned_count += deleted_count
                self.kept_count += 1
                
                logger.info(f"content_id {content_id}: {deleted_count}건 삭제, 1건 유지")
            
            logger.info(f"✅ 중복 레코드 정리 완료!")
            logger.info(f"�� 처리 결과: {self.cleaned_count}건 삭제, {self.kept_count}개 그룹에서 1건씩 유지")
            
            return True
            
        except Exception as e:
            logger.error(f"중복 레코드 정리 중 오류 발생: {str(e)}")
            return False
    
    def get_cleanup_summary(self) -> Dict[str, int]:
        """정리 전후 통계 조회"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            # 전체 레코드 수
            cursor.execute("SELECT COUNT(*) FROM spots")
            total_count = cursor.fetchone()[0]
            
            # content_id별 중복 수
            cursor.execute("""
                SELECT content_id, COUNT(*) as count
                FROM spots 
                WHERE content_id IS NOT NULL AND content_id != ''
                GROUP BY content_id
                HAVING COUNT(*) > 1
            """)
            duplicate_groups = cursor.fetchall()
            
            duplicate_count = sum(count for _, count in duplicate_groups)
            
            return {
                'total_records': total_count,
                'duplicate_groups': len(duplicate_groups),
                'duplicate_records': duplicate_count
            }
            
        except Error as e:
            logger.error(f"통계 조회 실패: {e}")
            return {}
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
    
    # 정리 전 통계
    cleaner = DuplicateSpotsCleaner(db_config)
    before_stats = cleaner.get_cleanup_summary()
    
    if before_stats:
        logger.info("=== 정리 전 통계 ===")
        logger.info(f"전체 레코드 수: {before_stats['total_records']:,}")
        logger.info(f"중복 그룹 수: {before_stats['duplicate_groups']:,}")
        logger.info(f"중복 레코드 수: {before_stats['duplicate_records']:,}")
        logger.info("")
    
    # 중복 레코드 정리 실행
    success = cleaner.clean_duplicates()
    
    if success:
        # 정리 후 통계
        after_stats = cleaner.get_cleanup_summary()
        if after_stats:
            logger.info("=== 정리 후 통계 ===")
            logger.info(f"전체 레코드 수: {after_stats['total_records']:,}")
            logger.info(f"중복 그룹 수: {after_stats['duplicate_groups']:,}")
            logger.info(f"중복 레코드 수: {after_stats['duplicate_records']:,}")
            
            if before_stats:
                cleaned = before_stats['duplicate_records'] - after_stats['duplicate_records']
                logger.info(f"정리된 레코드 수: {cleaned:,}")

if __name__ == "__main__":
    main()