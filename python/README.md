# 서울 관광 데이터 수집기

한국관광공사 Tour API를 사용하여 서울 관광지 데이터를 수집하고 MySQL DB에 저장하는 Python 스크립트입니다.

## 설치 및 설정

### 1. 가상환경 활성화
```bash
cd python
source venv/bin/activate  # macOS/Linux
# 또는
venv\Scripts\activate     # Windows
```

### 2. 필요한 패키지 설치
```bash
pip install -r requirements.txt
```

### 3. 환경변수 설정
프로젝트 루트에 `.env` 파일을 생성하고 다음 내용을 입력하세요:

```env
# Tour API Key
TOUR_API_KEY=your_tour_api_key_here

# MySQL Database Configuration
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USER=your_mysql_username
MYSQL_PASSWORD=your_mysql_password
MYSQL_DATABASE=flik_db
```

## 실행 방법

```bash
python tour_api_soeul.py
```

## 기능

- **비동기 데이터 수집**: aiohttp를 사용한 병렬 API 호출
- **다양한 관광지 타입**: 관광지, 문화시설, 축제공연행사, 여행코스, 레포츠, 숙박, 쇼핑, 음식점
- **MySQL DB 저장**: 수집된 데이터를 자동으로 DB에 저장
- **중복 데이터 처리**: ON DUPLICATE KEY UPDATE를 사용한 중복 방지
- **백업 파일 생성**: CSV, JSON 형태로 백업 파일 자동 생성
- **로깅**: 상세한 수집 과정 로그 출력

## 수집되는 데이터

- content_id: 관광지 고유 ID
- content_type_id/name: 관광지 타입
- title: 관광지명
- addr1/addr2: 주소
- first_image/first_image2: 이미지 URL
- map_x/map_y: 좌표
- area_code/sigungu_code: 지역 코드
- tel: 전화번호
- overview: 상세 설명
- raw_data: 원본 API 응답 데이터 (JSON)

## DB 테이블 구조

`tourism_spots` 테이블이 자동으로 생성되며, 다음과 같은 인덱스가 설정됩니다:
- content_type_id: 관광지 타입별 검색
- area_code, sigungu_code: 지역별 검색
- title: 관광지명 검색
