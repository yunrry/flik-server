import mysql.connector
from mysql.connector import Error
import os
from dotenv import load_dotenv
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def add_detail_columns():
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
    
    try:
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()
        
        # 공통 컬럼 추가 (모든 테이블)
        common_columns = [
            "ADD COLUMN usetime VARCHAR(200) DEFAULT ''",
            "ADD COLUMN restdate VARCHAR(200) DEFAULT ''",
            "ADD COLUMN parking VARCHAR(50) DEFAULT ''",
            "ADD COLUMN parkingfee VARCHAR(100) DEFAULT ''",
            "ADD COLUMN infocenter VARCHAR(200) DEFAULT ''",
            "ADD COLUMN chkbabycarriage VARCHAR(50) DEFAULT ''",
            "ADD COLUMN chkpet VARCHAR(50) DEFAULT ''",
            "ADD COLUMN chkcreditcard VARCHAR(50) DEFAULT ''"
        ]
        
        # 도메인별 특화 컬럼
        domain_columns = {
            "fetched_tourist_attractions": [
                "ADD COLUMN heritage1 VARCHAR(10) DEFAULT ''",
                "ADD COLUMN heritage2 VARCHAR(10) DEFAULT ''",
                "ADD COLUMN heritage3 VARCHAR(10) DEFAULT ''",
                "ADD COLUMN opendate VARCHAR(50) DEFAULT ''",
                "ADD COLUMN expguide TEXT",
                "ADD COLUMN expagerange VARCHAR(100) DEFAULT ''",
                "ADD COLUMN accomcount VARCHAR(50) DEFAULT ''",
                "ADD COLUMN useseason VARCHAR(200) DEFAULT ''"
            ],
            "fetched_cultural_facilities": [
                "ADD COLUMN scale VARCHAR(100) DEFAULT ''",
                "ADD COLUMN usefee VARCHAR(200) DEFAULT ''",
                "ADD COLUMN discountinfo VARCHAR(200) DEFAULT ''",
                "ADD COLUMN spendtime VARCHAR(100) DEFAULT ''"
            ],
            "fetched_festivals_events": [
                "ADD COLUMN sponsor1 VARCHAR(200) DEFAULT ''",
                "ADD COLUMN sponsor1tel VARCHAR(50) DEFAULT ''",
                "ADD COLUMN sponsor2 VARCHAR(200) DEFAULT ''",
                "ADD COLUMN sponsor2tel VARCHAR(50) DEFAULT ''",
                "ADD COLUMN eventenddate VARCHAR(20) DEFAULT ''",
                "ADD COLUMN playtime VARCHAR(100) DEFAULT ''",
                "ADD COLUMN eventplace VARCHAR(500) DEFAULT ''",
                "ADD COLUMN eventhomepage VARCHAR(500) DEFAULT ''",
                "ADD COLUMN agelimit VARCHAR(100) DEFAULT ''",
                "ADD COLUMN bookingplace VARCHAR(200) DEFAULT ''",
                "ADD COLUMN placeinfo TEXT",
                "ADD COLUMN subevent TEXT",
                "ADD COLUMN program TEXT",
                "ADD COLUMN eventstartdate VARCHAR(20) DEFAULT ''",
                "ADD COLUMN usetimefestival VARCHAR(200) DEFAULT ''",
                "ADD COLUMN discountinfofestival VARCHAR(200) DEFAULT ''",
                "ADD COLUMN spendtimefestival VARCHAR(100) DEFAULT ''",
                "ADD COLUMN festivalgrade VARCHAR(50) DEFAULT ''",
                "ADD COLUMN progresstype VARCHAR(50) DEFAULT ''",
                "ADD COLUMN festivaltype VARCHAR(50) DEFAULT ''"
            ],
            "fetched_sports_recreation": [
                "ADD COLUMN openperiod VARCHAR(200) DEFAULT ''",
                "ADD COLUMN reservation VARCHAR(200) DEFAULT ''",
                "ADD COLUMN scaleleports VARCHAR(100) DEFAULT ''",
                "ADD COLUMN accomcountleports VARCHAR(50) DEFAULT ''",
                "ADD COLUMN usefeeleports VARCHAR(200) DEFAULT ''",
                "ADD COLUMN expagerangeleports VARCHAR(100) DEFAULT ''"
            ],
            "fetched_accommodations": [
                "ADD COLUMN roomcount VARCHAR(50) DEFAULT ''",
                "ADD COLUMN roomtype VARCHAR(200) DEFAULT ''",
                "ADD COLUMN refundregulation TEXT",
                "ADD COLUMN checkintime VARCHAR(20) DEFAULT ''",
                "ADD COLUMN checkouttime VARCHAR(20) DEFAULT ''",
                "ADD COLUMN chkcooking VARCHAR(50) DEFAULT ''",
                "ADD COLUMN seminar VARCHAR(10) DEFAULT ''",
                "ADD COLUMN sports VARCHAR(10) DEFAULT ''",
                "ADD COLUMN sauna VARCHAR(10) DEFAULT ''",
                "ADD COLUMN beauty VARCHAR(10) DEFAULT ''",
                "ADD COLUMN beverage VARCHAR(10) DEFAULT ''",
                "ADD COLUMN karaoke VARCHAR(10) DEFAULT ''",
                "ADD COLUMN barbecue VARCHAR(10) DEFAULT ''",
                "ADD COLUMN campfire VARCHAR(10) DEFAULT ''",
                "ADD COLUMN bicycle VARCHAR(10) DEFAULT ''",
                "ADD COLUMN fitness VARCHAR(10) DEFAULT ''",
                "ADD COLUMN publicpc VARCHAR(10) DEFAULT ''",
                "ADD COLUMN publicbath VARCHAR(10) DEFAULT ''",
                "ADD COLUMN subfacility TEXT",
                "ADD COLUMN foodplace VARCHAR(200) DEFAULT ''",
                "ADD COLUMN reservationurl VARCHAR(500) DEFAULT ''",
                "ADD COLUMN pickup VARCHAR(200) DEFAULT ''",
                "ADD COLUMN reservationlodging VARCHAR(200) DEFAULT ''",
                "ADD COLUMN scalelodging VARCHAR(100) DEFAULT ''",
                "ADD COLUMN accomcountlodging VARCHAR(50) DEFAULT ''"
            ],
            "fetched_shopping": [
                "ADD COLUMN saleitem TEXT",
                "ADD COLUMN saleitemcost VARCHAR(200) DEFAULT ''",
                "ADD COLUMN fairday VARCHAR(200) DEFAULT ''",
                "ADD COLUMN opendateshopping VARCHAR(50) DEFAULT ''",
                "ADD COLUMN shopguide TEXT",
                "ADD COLUMN culturecenter VARCHAR(200) DEFAULT ''",
                "ADD COLUMN restroom VARCHAR(50) DEFAULT ''",
                "ADD COLUMN scaleshopping VARCHAR(100) DEFAULT ''"
            ],
            "fetched_restaurants": [
                "ADD COLUMN seat VARCHAR(50) DEFAULT ''",
                "ADD COLUMN kidsfacility VARCHAR(10) DEFAULT ''",
                "ADD COLUMN firstmenu TEXT",
                "ADD COLUMN treatmenu TEXT",
                "ADD COLUMN smoking VARCHAR(50) DEFAULT ''",
                "ADD COLUMN packing VARCHAR(50) DEFAULT ''",
                "ADD COLUMN scalefood VARCHAR(100) DEFAULT ''",
                "ADD COLUMN opendatefood VARCHAR(50) DEFAULT ''",
                "ADD COLUMN discountinfofood VARCHAR(200) DEFAULT ''",
                "ADD COLUMN reservationfood VARCHAR(200) DEFAULT ''",
                "ADD COLUMN lcnsno VARCHAR(50) DEFAULT ''"
            ]
        }
        
        tables = [
            "fetched_tourist_attractions",
            "fetched_cultural_facilities", 
            "fetched_festivals_events",
            "fetched_sports_recreation",
            "fetched_accommodations",
            "fetched_shopping",
            "fetched_restaurants"
        ]
        
        for table in tables:
            logger.info(f"테이블 {table} 컬럼 추가 시작")
            
            try:
                # 공통 컬럼 추가
                for column in common_columns:
                    try:
                        cursor.execute(f"ALTER TABLE {table} {column}")
                        logger.info(f"  ✅ {column.split()[2]} 컬럼 추가")
                    except Error as e:
                        if "Duplicate column name" in str(e):
                            logger.warning(f"  ⚠️ {column.split()[2]} 컬럼이 이미 존재함")
                        else:
                            logger.error(f"  ❌ {column.split()[2]} 컬럼 추가 실패: {e}")
                
                # 도메인별 특화 컬럼 추가
                if table in domain_columns:
                    for column in domain_columns[table]:
                        try:
                            cursor.execute(f"ALTER TABLE {table} {column}")
                            logger.info(f"  ✅ {column.split()[2]} 컬럼 추가")
                        except Error as e:
                            if "Duplicate column name" in str(e):
                                logger.warning(f"  ⚠️ {column.split()[2]} 컬럼이 이미 존재함")
                            else:
                                logger.error(f"  ❌ {column.split()[2]} 컬럼 추가 실패: {e}")
                
                logger.info(f"테이블 {table} 컬럼 추가 완료")
                
            except Error as e:
                logger.error(f"테이블 {table} 컬럼 추가 실패: {e}")
        
        connection.commit()
        logger.info("모든 테이블 컬럼 추가 완료")
        
    except Error as e:
        logger.error(f"작업 실패: {e}")
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()

if __name__ == "__main__":
    add_detail_columns()