package yunrry.flik.core.domain.testfixture;

import yunrry.flik.core.domain.model.card.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public class SpotTestFixture {

    public static Restaurant createTestRestaurant() {
        return new Restaurant(
                1L, // id
                "테스트 음식점", // name
                "39", // contentTypeId
                "한식", // category
                "테스트용 음식점입니다", // description
                "서울시 강남구", // address
                "11", // regnCd
                "11680", // sigunguCd
                BigDecimal.valueOf(342.3425),// latitude
                BigDecimal.valueOf(123.1234), // longitude
                List.of("test1.jpg", "test2.jpg"), // imageUrls
                "02-1234-5678", // call
                BigDecimal.valueOf(4.5), // rating
                "35436638", // googlePlaceId
                150, // reviewCount
                "맛집", // tag1
                "가족식사", // tag2
                "데이트", // tag3
                "맛집,가족식사,데이트", // tags
                "가능", // parking
                "불가", // petCarriage
                "가능", // babyCarriage
                LocalTime.of(9, 0), // openTime
                LocalTime.of(22, 0), // closeTime
                "매일 08:00 ~ 18:00", // time
                "월", // dayOff
                "한식", // cuisineType
                "10,000-20,000원", // priceRange
                "가능", // reservation
                "있음", // kidsFacility
                "가능", // takeAway
                "비빔밥", // firstMenu
                "김치찌개" // treatMenu
        );
    }

    public static Accomodation createTestAccomodation() {
        return new Accomodation(
                1L, // id
                "테스트 숙박시설", // name
                "32", // contentTypeId
                "호텔", // category
                "테스트용 숙박시설입니다", // description
                "서울시 강남구", // address
                "11", // regnCd
                "11680", // sigunguCd
                BigDecimal.valueOf(342.3425),// latitude
                BigDecimal.valueOf(123.1234), // longitude
                List.of("hotel1.jpg", "hotel2.jpg"), // imageUrls
                "02-1234-5678", // call
                BigDecimal.valueOf(4.2), // rating
                "35436638", // googlePlaceId
                150, // reviewCount
                "숙박", // tag1
                "호텔", // tag2
                "출장", // tag3
                "숙박,호텔,출장", // tags
                "가능", // parking
                "불가", // petCarriage
                "가능", // babyCarriage
                LocalTime.of(15, 0), // openTime (체크인)
                LocalTime.of(11, 0), // closeTime (체크아웃)
                "매일 08:00 ~ 18:00", // time
                "연중무휴", // dayOff
                "가능", // reservation
                "15:00", // checkInTime
                "11:00", // checkOutTime
                true, // cooking
                "Wi-Fi, 수영장, 피트니스" // facilities
        );
    }

    public static Cultural createTestCultural() {
        return new Cultural(
                1L, // id
                "테스트 문화시설", // name
                "14", // contentTypeId
                "박물관", // category
                "테스트용 문화시설입니다", // description
                "서울시 종로구", // address
                "11", // regnCd
                "11110", // sigunguCd
                BigDecimal.valueOf(342.3425),// latitude
                BigDecimal.valueOf(123.1234), // longitude
                List.of("museum1.jpg", "museum2.jpg"),// imageUrls
                "02-1234-5678", // call
                BigDecimal.valueOf(4.3), // rating
                "35436638", // googlePlaceId
                150, // reviewCount
                "문화", // tag1
                "박물관", // tag2
                "교육", // tag3
                "문화,박물관,교육", // tags
                "가능", // parking
                "가능", // petCarriage
                "가능", // babyCarriage
                LocalTime.of(9, 0), // openTime
                LocalTime.of(18, 0), // closeTime
                "매일 08:00 ~ 18:00", // time
                "월요일", // dayOff
                "성인 5,000원, 청소년 3,000원" // fee
        );
    }

    public static Festival createTestFestival() {
        return new Festival(
                1L, // id
                "테스트 축제", // name
                "15", // contentTypeId
                "문화축제", // category
                "테스트용 축제입니다", // description
                "서울시 중구", // address
                "11", // regnCd
                "11140", // sigunguCd
                BigDecimal.valueOf(342.3425),// latitude
                BigDecimal.valueOf(123.1234), // longitude
                List.of("festival1.jpg", "festival2.jpg"), // imageUrls
                "02-1234-5678", // call
                BigDecimal.valueOf(4.6), // rating
                "35436638", // googlePlaceId
                150, // reviewCount
                "축제", // tag1
                "문화", // tag2
                "이벤트", // tag3
                "축제,문화,이벤트", // tags
                "가능", // parking
                "가능", // petCarriage
                "가능", // babyCarriage
                LocalTime.of(10, 0), // openTime
                LocalTime.of(22, 0), // closeTime
                "매일 08:00 ~ 18:00", // time
                "없음", // dayOff
                "2024-05-01", // eventStartDate
                "2024-05-07", // eventEndDate
                "전체관람가", // ageLimit
                "서울시", // sponsor
                "120분", // runningTime
                "무료" // fee
        );
    }

    public static Leisure createTestLeisure() {
        return new Leisure(
                1L, // id
                "테스트 레저시설", // name
                "28", // contentTypeId
                "스포츠", // category
                "테스트용 레저시설입니다", // description
                "서울시 송파구", // address
                "11", // regnCd
                "11240", // sigunguCd
                BigDecimal.valueOf(342.3425),// latitude
                BigDecimal.valueOf(123.1234), // longitude
                List.of("leisure1.jpg", "leisure2.jpg"), // imageUrls
                "02-1234-5678", // call
                BigDecimal.valueOf(4.4), // rating
                "35436638", // googlePlaceId
                150, // reviewCount
                "레저", // tag1
                "스포츠", // tag2
                "운동", // tag3
                "레저,스포츠,운동", // tags
                "가능", // parking
                "불가", // petCarriage
                "가능", // babyCarriage
                LocalTime.of(6, 0), // openTime
                LocalTime.of(23, 0), // closeTime
                "매일 08:00 ~ 18:00", // time
                "없음", // dayOff
                "성인 15,000원, 청소년 10,000원" // fee
        );
    }

    public static Shop createTestShop() {
        return new Shop(
                1L, // id
                "테스트 쇼핑몰", // name
                "38", // contentTypeId
                "쇼핑센터", // category
                "테스트용 쇼핑몰입니다", // description
                "서울시 강남구", // address
                "11", // regnCd
                "11680", // sigunguCd
                BigDecimal.valueOf(342.3425),// latitude
                BigDecimal.valueOf(123.1234), // longitude
                List.of("shop1.jpg", "shop2.jpg"), // imageUrls
                "02-1234-5678", // call
                BigDecimal.valueOf(4.1), // rating
                "35436638", // googlePlaceId
                150, // reviewCount
                "쇼핑", // tag1
                "패션", // tag2
                "브랜드", // tag3
                "쇼핑,패션,브랜드", // tags
                "가능", // parking
                "가능", // petCarriage
                "가능", // babyCarriage
                LocalTime.of(10, 0), // openTime
                LocalTime.of(22, 0), // closeTime
                "매일 08:00 ~ 18:00", // time
                "없음", // dayOff
                "의류, 화장품, 액세서리" // products
        );
    }

    public static TourSpot createTestTourSpot() {
        return new TourSpot(
                1L, // id
                "테스트 관광지", // name
                "12", // contentTypeId
                "자연명소", // category
                "테스트용 관광지입니다", // description
                "제주도 제주시", // address
                "50", // regnCd
                "50110", // sigunguCd
                BigDecimal.valueOf(342.3425),// latitude
                BigDecimal.valueOf(123.1234), // longitude
                List.of("tour1.jpg", "tour2.jpg"), // imageUrls
                "064-1234-5678", // call
                BigDecimal.valueOf(4.8), // rating
                "35436638", // googlePlaceId
                150, // reviewCount
                "관광", // tag1
                "자연", // tag2
                "명소", // tag3
                "관광,자연,명소", // tags
                "가능", // parking
                "가능", // petCarriage
                "가능", // babyCarriage
                LocalTime.of(8, 0), // openTime
                LocalTime.of(18, 0), // closeTime
                "매일 08:00 ~ 18:00", // time
                "없음", // dayOff
                "아름다운 자연경관을 감상할 수 있습니다", // expGuide
                "전체관람가" // ageLimit
        );
    }

}