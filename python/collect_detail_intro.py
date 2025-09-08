import asyncio
import aiohttp
import mysql.connector
from mysql.connector import Error
import os
from dotenv import load_dotenv
import logging
from typing import List, Dict, Any
import time

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class DetailIntroDataCollector:
    def __init__(self, service_key: str, db_config: Dict[str, str]):
        self.service_key = service_key
        self.db_config = db_config
        self.base_url = "http://apis.data.go.kr/B551011/KorService2/detailIntro2"
        self.table_mapping = {
            # "12": "fetched_tourist_attractions",
            # "14": "fetched_cultural_facilities", 
            "15": "fetched_festivals_events",
            # "28": "fetched_sports_recreation",
            # "32": "fetched_accommodations",
            # "38": "fetched_shopping",
            # "39": "fetched_restaurants"
        }
        
    async def fetch_detail_intro(self, session: aiohttp.ClientSession, content_id: str, content_type_id: str) -> Dict[str, Any]:
        """detailIntro2 API 호출"""
        params = {
            "serviceKey": self.service_key,
            "MobileOS": "WEB",
            "MobileApp": "Flik",
            "_type": "json",
            "contentId": content_id,
            "contentTypeId": content_type_id
        }
        
        try:
            async with session.get(self.base_url, params=params) as response:
                if response.status == 200:
                    data = await response.json()
                    return {
                        "content_id": content_id,
                        "content_type_id": content_type_id,
                        "data": data,
                        "success": True
                    }
                else:
                    logger.error(f"API 호출 실패 - Content ID: {content_id}, Status: {response.status}")
                    return {"content_id": content_id, "content_type_id": content_type_id, "success": False}
                    
        except Exception as e:
            logger.error(f"API 호출 중 오류 - Content ID: {content_id}, Error: {str(e)}")
            return {"content_id": content_id, "content_type_id": content_type_id, "success": False}
    
    def parse_detail_data(self, item: Dict[str, Any], content_type_id: str) -> Dict[str, Any]:
        """API 응답 데이터를 테이블별로 파싱"""
        parsed_data = {
            "content_id": item.get("contentid", ""),
            "content_type_id": content_type_id
        }
        
        # 공통 컬럼 매핑
        common_mappings = {
            "usetime": ["usetime", "usetimeculture", "usetimeleports", "opentime", "opentimefood"],
            "restdate": ["restdate", "restdateculture", "restdateleports", "restdateshopping", "restdatefood"],
            "parking": ["parking", "parkingculture", "parkingleports", "parkinglodging", "parkingshopping", "parkingfood"],
            "parkingfee": ["parkingfeeleports"],
            "infocenter": ["infocenter", "infocenterculture", "infocenterleports", "infocenterlodging", "infocentershopping", "infocenterfood"],
            "chkbabycarriage": ["chkbabycarriage", "chkbabycarriageculture", "chkbabycarriageleports", "chkbabycarriageshopping"],
            "chkpet": ["chkpet", "chkpetculture", "chkpetshopping"],
            "chkcreditcard": ["chkcreditcard", "chkcreditcardculture", "chkcreditcardleports", "chkcreditcardshopping", "chkcreditcardfood"]
        }
        
        # 공통 컬럼 값 추출
        for common_key, api_keys in common_mappings.items():
            value = ""
            for api_key in api_keys:
                if api_key in item and item[api_key]:
                    value = item[api_key]
                    break
            parsed_data[common_key] = value
        
        # 도메인별 특화 컬럼 매핑
        if content_type_id == "12":  # 관광지
            domain_fields = [
                "heritage1", "heritage2", "heritage3", "opendate", "expguide", 
                "expagerange", "accomcount", "useseason"
            ]
        elif content_type_id == "14":  # 문화시설
            domain_fields = [
                "scale", "usefee", "discountinfo", "spendtime"
            ]
        elif content_type_id == "15":  # 축제공연행사
            domain_fields = [
                "sponsor1", "sponsor1tel", "sponsor2", "sponsor2tel", "eventenddate",
                "playtime", "eventplace", "eventhomepage", "agelimit", "bookingplace",
                "placeinfo", "subevent", "program", "eventstartdate", "usetimefestival",
                "discountinfofestival", "spendtimefestival", "festivalgrade", 
                "progresstype", "festivaltype"
            ]
        elif content_type_id == "28":  # 레포츠
            domain_fields = [
                "openperiod", "reservation", "scaleleports", "accomcountleports",
                "usefeeleports", "expagerangeleports"
            ]
        elif content_type_id == "32":  # 숙박
            domain_fields = [
                "roomcount", "roomtype", "refundregulation", "checkintime", "checkouttime",
                "chkcooking", "seminar", "sports", "sauna", "beauty", "beverage", "karaoke",
                "barbecue", "campfire", "bicycle", "fitness", "publicpc", "publicbath",
                "subfacility", "foodplace", "reservationurl", "pickup", "reservationlodging",
                "scalelodging", "accomcountlodging"
            ]
        elif content_type_id == "38":  # 쇼핑
            domain_fields = [
                "saleitem", "saleitemcost", "fairday", "opendateshopping", "shopguide",
                "culturecenter", "restroom", "scaleshopping"
            ]
        elif content_type_id == "39":  # 음식점
            domain_fields = [
                "seat", "kidsfacility", "firstmenu", "treatmenu", "smoking", "packing",
                "scalefood", "opendatefood", "discountinfofood", "reservationfood", "lcnsno"
            ]
        else:
            domain_fields = []
        
        # 도메인별 필드 값 추출
        for field in domain_fields:
            parsed_data[field] = item.get(field, "")
        
        return parsed_data
    
    def get_content_ids(self, content_type_id: str) -> List[str]:
        """특정 content_type_id에 해당하는 content_id 목록 조회"""
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            table_name = self.table_mapping[content_type_id]
            query = f"SELECT content_id FROM {table_name} ORDER BY id"
            cursor.execute(query)
            results = cursor.fetchall()
            
            content_ids = [row[0] for row in results]
            logger.info(f"{table_name}에서 {len(content_ids)}개의 content_id 조회")
            return content_ids
            
        except Error as e:
            logger.error(f"content_id 조회 실패: {e}")
            return []
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def update_table_with_detail_data(self, content_type_id: str, detail_data: List[Dict[str, Any]]) -> bool:
        """테이블에 상세 정보 업데이트"""
        if not detail_data:
            return True
            
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()
            
            table_name = self.table_mapping[content_type_id]
            
            # 공통 컬럼 업데이트 쿼리
            common_update_query = f"""
            UPDATE {table_name} SET
                usetime = %s,
                restdate = %s,
                parking = %s,
                parkingfee = %s,
                infocenter = %s,
                chkbabycarriage = %s,
                chkpet = %s,
                chkcreditcard = %s
            WHERE content_id = %s
            """
            
            # 도메인별 특화 컬럼 업데이트
            domain_update_queries = {
                "12": f"""
                UPDATE {table_name} SET
                    heritage1 = %s, heritage2 = %s, heritage3 = %s, opendate = %s,
                    expguide = %s, expagerange = %s, accomcount = %s, useseason = %s
                WHERE content_id = %s
                """,
                "14": f"""
                UPDATE {table_name} SET
                    scale = %s, usefee = %s, discountinfo = %s, spendtime = %s
                WHERE content_id = %s
                """,
                "15": f"""
                UPDATE {table_name} SET
                    sponsor1 = %s, sponsor1tel = %s, sponsor2 = %s, sponsor2tel = %s,
                    eventenddate = %s, playtime = %s, eventplace = %s, eventhomepage = %s,
                    agelimit = %s, bookingplace = %s, placeinfo = %s, subevent = %s,
                    program = %s, eventstartdate = %s, usetimefestival = %s,
                    discountinfofestival = %s, spendtimefestival = %s, festivalgrade = %s,
                    progresstype = %s, festivaltype = %s
                WHERE content_id = %s
                """,
                "28": f"""
                UPDATE {table_name} SET
                    openperiod = %s, reservation = %s, scaleleports = %s, accomcountleports = %s,
                    usefeeleports = %s, expagerangeleports = %s
                WHERE content_id = %s
                """,
                "32": f"""
                UPDATE {table_name} SET
                    roomcount = %s, roomtype = %s, refundregulation = %s, checkintime = %s,
                    checkouttime = %s, chkcooking = %s, seminar = %s, sports = %s, sauna = %s,
                    beauty = %s, beverage = %s, karaoke = %s, barbecue = %s, campfire = %s,
                    bicycle = %s, fitness = %s, publicpc = %s, publicbath = %s, subfacility = %s,
                    foodplace = %s, reservationurl = %s, pickup = %s, reservationlodging = %s,
                    scalelodging = %s, accomcountlodging = %s
                WHERE content_id = %s
                """,
                "38": f"""
                UPDATE {table_name} SET
                    saleitem = %s, saleitemcost = %s, fairday = %s, opendateshopping = %s,
                    shopguide = %s, culturecenter = %s, restroom = %s, scaleshopping = %s
                WHERE content_id = %s
                """,
                "39": f"""
                UPDATE {table_name} SET
                    seat = %s, kidsfacility = %s, firstmenu = %s, treatmenu = %s, smoking = %s,
                    packing = %s, scalefood = %s, opendatefood = %s, discountinfofood = %s,
                    reservationfood = %s, lcnsno = %s
                WHERE content_id = %s
                """
            }
            
            # 공통 컬럼 업데이트
            for data in detail_data:
                common_values = (
                    data.get('usetime', ''),
                    data.get('restdate', ''),
                    data.get('parking', ''),
                    data.get('parkingfee', ''),
                    data.get('infocenter', ''),
                    data.get('chkbabycarriage', ''),
                    data.get('chkpet', ''),
                    data.get('chkcreditcard', ''),
                    data.get('content_id', '')
                )
                cursor.execute(common_update_query, common_values)
            
            # 도메인별 특화 컬럼 업데이트
            domain_query = domain_update_queries.get(content_type_id)
            if domain_query:
                for data in detail_data:
                    if content_type_id == "12":
                        domain_values = (
                            data.get('heritage1', ''), data.get('heritage2', ''), data.get('heritage3', ''),
                            data.get('opendate', ''), data.get('expguide', ''), data.get('expagerange', ''),
                            data.get('accomcount', ''), data.get('useseason', ''), data.get('content_id', '')
                        )
                    elif content_type_id == "14":
                        domain_values = (
                            data.get('scale', ''), data.get('usefee', ''), data.get('discountinfo', ''),
                            data.get('spendtime', ''), data.get('content_id', '')
                        )
                    elif content_type_id == "15":
                        domain_values = (
                            data.get('sponsor1', ''), data.get('sponsor1tel', ''), data.get('sponsor2', ''),
                            data.get('sponsor2tel', ''), data.get('eventenddate', ''), data.get('playtime', ''),
                            data.get('eventplace', ''), data.get('eventhomepage', ''), data.get('agelimit', ''),
                            data.get('bookingplace', ''), data.get('placeinfo', ''), data.get('subevent', ''),
                            data.get('program', ''), data.get('eventstartdate', ''), data.get('usetimefestival', ''),
                            data.get('discountinfofestival', ''), data.get('spendtimefestival', ''),
                            data.get('festivalgrade', ''), data.get('progresstype', ''), data.get('festivaltype', ''),
                            data.get('content_id', '')
                        )
                    elif content_type_id == "28":
                        domain_values = (
                            data.get('openperiod', ''), data.get('reservation', ''), data.get('scaleleports', ''),
                            data.get('accomcountleports', ''), data.get('usefeeleports', ''),
                            data.get('expagerangeleports', ''), data.get('content_id', '')
                        )
                    elif content_type_id == "32":
                        domain_values = (
                            data.get('roomcount', ''), data.get('roomtype', ''), data.get('refundregulation', ''),
                            data.get('checkintime', ''), data.get('checkouttime', ''), data.get('chkcooking', ''),
                            data.get('seminar', ''), data.get('sports', ''), data.get('sauna', ''),
                            data.get('beauty', ''), data.get('beverage', ''), data.get('karaoke', ''),
                            data.get('barbecue', ''), data.get('campfire', ''), data.get('bicycle', ''),
                            data.get('fitness', ''), data.get('publicpc', ''), data.get('publicbath', ''),
                            data.get('subfacility', ''), data.get('foodplace', ''), data.get('reservationurl', ''),
                            data.get('pickup', ''), data.get('reservationlodging', ''), data.get('scalelodging', ''),
                            data.get('accomcountlodging', ''), data.get('content_id', '')
                        )
                    elif content_type_id == "38":
                        domain_values = (
                            data.get('saleitem', ''), data.get('saleitemcost', ''), data.get('fairday', ''),
                            data.get('opendateshopping', ''), data.get('shopguide', ''), data.get('culturecenter', ''),
                            data.get('restroom', ''), data.get('scaleshopping', ''), data.get('content_id', '')
                        )
                    elif content_type_id == "39":
                        domain_values = (
                            data.get('seat', ''), data.get('kidsfacility', ''), data.get('firstmenu', ''),
                            data.get('treatmenu', ''), data.get('smoking', ''), data.get('packing', ''),
                            data.get('scalefood', ''), data.get('opendatefood', ''), data.get('discountinfofood', ''),
                            data.get('reservationfood', ''), data.get('lcnsno', ''), data.get('content_id', '')
                        )
                    
                    cursor.execute(domain_query, domain_values)
            
            connection.commit()
            logger.info(f"{table_name} 상세 정보 업데이트 완료: {len(detail_data)}건")
            return True
            
        except Error as e:
            logger.error(f"상세 정보 업데이트 실패: {e}")
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    async def process_content_type(self, content_type_id: str, max_items: int = 50) -> bool:
        """특정 content_type의 상세 정보 수집 및 업데이트"""
        logger.info(f"Content Type {content_type_id} 상세 정보 수집 시작")
        
        # content_id 목록 조회
        content_ids = self.get_content_ids(content_type_id)
        if not content_ids:
            logger.warning(f"Content Type {content_type_id}에 해당하는 데이터가 없습니다.")
            return True
        
        # 최대 처리 개수 제한
        content_ids = content_ids[:max_items]
        
        async with aiohttp.ClientSession() as session:
            detail_data = []
            
            for i, content_id in enumerate(content_ids):
                # API 호출
                result = await self.fetch_detail_intro(session, content_id, content_type_id)
                
                if result["success"]:
                    response_data = result["data"]
                    
                    # API 응답 구조 확인
                    if "response" in response_data and "body" in response_data["response"]:
                        body = response_data["response"]["body"]
                        items = body.get("items", {})
                        
                        if items and "item" in items:
                            item_list = items["item"]
                            if isinstance(item_list, dict):
                                item_list = [item_list]
                            
                            for item in item_list:
                                parsed_item = self.parse_detail_data(item, content_type_id)
                                detail_data.append(parsed_item)
                
                # 진행 상황 로그
                if (i + 1) % 10 == 0:
                    logger.info(f"진행률: {i + 1}/{len(content_ids)} ({((i + 1)/len(content_ids)*100):.1f}%)")
                
                # API 호출 제한을 위한 딜레이
                await asyncio.sleep(0.1)
            
            # DB 업데이트
            if detail_data:
                return self.update_table_with_detail_data(content_type_id, detail_data)
            else:
                logger.warning(f"Content Type {content_type_id}에 대한 상세 정보가 없습니다.")
                return True
    
    async def process_all_content_types(self, max_items_per_type: int = 50):
        """모든 content_type의 상세 정보 수집"""
        logger.info("모든 Content Type 상세 정보 수집 시작")
        
        for content_type_id in self.table_mapping.keys():
            try:
                success = await self.process_content_type(content_type_id, max_items_per_type)
                if success:
                    logger.info(f"✅ Content Type {content_type_id} 처리 완료")
                else:
                    logger.error(f"❌ Content Type {content_type_id} 처리 실패")
            except Exception as e:
                logger.error(f"Content Type {content_type_id} 처리 중 오류: {str(e)}")
        
        logger.info("모든 Content Type 상세 정보 수집 완료")


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
    
    # 상세 정보 수집기 초기화
    collector = DetailIntroDataCollector(SERVICE_KEY, db_config)
    
    try:
        # 모든 content_type의 상세 정보 수집 (타입당 최대 50개)
        await collector.process_all_content_types(max_items_per_type=50)
        logger.info("상세 정보 수집 작업 완료!")
        
    except Exception as e:
        logger.error(f"작업 중 오류 발생: {str(e)}")


if __name__ == "__main__":
    asyncio.run(main())