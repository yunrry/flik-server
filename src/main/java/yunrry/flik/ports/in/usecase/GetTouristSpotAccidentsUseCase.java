//package yunrry.flik.application.usecase;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import yys.safewalk.application.port.in.GetTouristSpotAccidentsQuery;
//import yys.safewalk.application.port.out.LoadPopularTouristSpotPort;
//import yys.safewalk.application.port.out.LoadAccidentHotspotsPort;
//import yys.safewalk.domain.model.*;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class GetTouristSpotAccidentsUseCase implements GetTouristSpotAccidentsQuery {
//
//    private final LoadPopularTouristSpotPort loadPopularTouristSpotPort;
//    private final LoadAccidentHotspotsPort loadAccidentHotspotsPort;
//
//    @Override
//    public TouristSpotAccidentResponse getAccidentsInRadius(String spotId, Integer radiusKm) {
//        // 기본 반경 5km 설정
//        int finalRadiusKm = (radiusKm != null) ? radiusKm : 5;
//
//        log.info("관광지 반경 내 교통사고 조회 시작: spotId={}, radiusKm={}", spotId, finalRadiusKm);
//
//        // 1. 관광지 정보 조회 - 첫 번째 결과만 사용
//        PopularTouristSpots touristSpot = loadPopularTouristSpotPort.loadById(spotId)
//                .orElseThrow(() -> new IllegalArgumentException("관광지를 찾을 수 없습니다: " + spotId));
//
//        if (touristSpot.latitude() == null || touristSpot.longitude() == null) {
//            log.warn("관광지 좌표가 없습니다: spotId={}", spotId);
//            return new TouristSpotAccidentResponse(
//                    touristSpot.spotName(),
//                    touristSpot.touristSpotId(),
//                    0,
//                    List.of()
//            );
//        }
//
//        // 2. 반경 내 교통사고 조회 (finalRadiusKm 사용)
//        List<AccidentDetail> accidents = loadAccidentHotspotsPort.findAccidentsInRadius(
//                touristSpot.latitude(),
//                touristSpot.longitude(),
//                finalRadiusKm  // radiusKm → finalRadiusKm으로 수정
//        );
//
//        // 3. 응답 생성
//        int totalAccident = accidents.stream()
//                .mapToInt(AccidentDetail::getAccidentCount)
//                .sum();
//
//        log.info("관광지 반경 내 교통사고 조회 완료: spotId={}, totalAccident={}", spotId, totalAccident);
//
//        return new TouristSpotAccidentResponse(
//                touristSpot.spotName(),
//                touristSpot.touristSpotId(),
//                totalAccident,
//                accidents
//        );
//    }
//
//    private AccidentDetail mapToAccidentDetail(Object accidentEntity) {
//        try {
//            String id = extractId(accidentEntity);
//            String location = extractLocation(accidentEntity);
//            Integer accidentCount = extractAccidentCount(accidentEntity);
//            Casualties casualties = extractCasualties(accidentEntity);
//            Coordinate point = extractCoordinate(accidentEntity);
//
//            return new AccidentDetail(id, location, accidentCount, casualties, point);
//        } catch (Exception e) {
//            log.error("사고 데이터 매핑 실패", e);
//            return new AccidentDetail("unknown", "위치 정보 없음", 0,
//                    new Casualties(0, 0, 0, 0),
//                    new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
//        }
//    }
//
//    private String extractId(Object entity) {
//        try {
//            var idField = entity.getClass().getMethod("getId");
//            return idField.invoke(entity).toString();
//        } catch (Exception e) {
//            log.error("ID 추출 실패", e);
//            return "unknown";
//        }
//    }
//
//    private String extractLocation(Object entity) {
//        try {
//            var locationField = entity.getClass().getMethod("getLocation");
//            return (String) locationField.invoke(entity);
//        } catch (Exception e) {
//            log.error("위치 추출 실패", e);
//            return "위치 정보 없음";
//        }
//    }
//
//    private Integer extractAccidentCount(Object entity) {
//        try {
//            var countField = entity.getClass().getMethod("getAccidentCount");
//            return (Integer) countField.invoke(entity);
//        } catch (Exception e) {
//            log.error("사고 건수 추출 실패", e);
//            return 0;
//        }
//    }
//
//    private Casualties extractCasualties(Object entity) {
//        try {
//            var deadField = entity.getClass().getMethod("getDead");
//            var severeField = entity.getClass().getMethod("getSevere");
//            var minorField = entity.getClass().getMethod("getMinor");
//
//            Integer dead = (Integer) deadField.invoke(entity);
//            Integer severe = (Integer) severeField.invoke(entity);
//            Integer minor = (Integer) minorField.invoke(entity);
//
//            int total = (dead != null ? dead : 0) +
//                       (severe != null ? severe : 0) +
//                       (minor != null ? minor : 0);
//
//            return new Casualties(total,
//                    dead != null ? dead : 0,
//                    severe != null ? severe : 0,
//                    minor != null ? minor : 0);
//        } catch (Exception e) {
//            log.error("사상자 정보 추출 실패", e);
//            return new Casualties(0, 0, 0, 0);
//        }
//    }
//
//    private Coordinate extractCoordinate(Object entity) {
//        try {
//            var latField = entity.getClass().getMethod("getLatitude");
//            var lngField = entity.getClass().getMethod("getLongitude");
//
//            BigDecimal lat = (BigDecimal) latField.invoke(entity);
//            BigDecimal lng = (BigDecimal) lngField.invoke(entity);
//
//            return new Coordinate(lat, lng);
//        } catch (Exception e) {
//            log.error("좌표 추출 실패", e);
//            return new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO);
//        }
//    }
//}
