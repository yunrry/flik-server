//package yunrry.flik.application.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import yys.safewalk.entity.PopularTouristSpotsEntity;
//import yys.safewalk.infrastructure.adapter.out.persistence.PopularTouristSpotsJPARepository;
//import yys.safewalk.infrastructure.external.NaverLocalSearchApiClient;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class TouristSpotCoordinateNaverService {
//
//    private final PopularTouristSpotsJPARepository popularTouristSpotsJPARepository;
//    private final NaverLocalSearchApiClient naverApiClient;
//
//    // 병렬 처리를 위한 스레드 풀 (네이버 API 제한 고려하여 크기 조정)
//    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
//
//    @Transactional
//    public void updateAllCoordinates() {
//        List<PopularTouristSpotsEntity> spotsWithoutCoordinates = popularTouristSpotsJPARepository.findByLongitudeIsNullOrLatitudeIsNull();
//
//        log.info("네이버 API로 좌표가 없는 관광지 {}개 발견", spotsWithoutCoordinates.size());
//
//        // 배치 크기 설정 (네이버 API 제한 고려)
//        int batchSize = 20;
//        int totalBatches = (int) Math.ceil((double) spotsWithoutCoordinates.size() / batchSize);
//
//        int successCount = 0;
//        int failCount = 0;
//
//        // 배치 단위로 처리
//        for (int i = 0; i < totalBatches; i++) {
//            int startIndex = i * batchSize;
//            int endIndex = Math.min(startIndex + batchSize, spotsWithoutCoordinates.size());
//            List<PopularTouristSpotsEntity> batch = spotsWithoutCoordinates.subList(startIndex, endIndex);
//
//            log.info("배치 {}/{} 처리 중... ({}-{})", i + 1, totalBatches, startIndex + 1, endIndex);
//
//            // 병렬 처리 (CompletableFuture 사용)
//            List<CompletableFuture<Void>> futures = batch.stream()
//                    .map(spot -> CompletableFuture.runAsync(() -> {
//                        try {
//                            updateCoordinate(spot);
//                        } catch (Exception e) {
//                            log.error("좌표 업데이트 실패: id={}, name={}, error={}",
//                                    spot.getId(), spot.getSpotName(), e.getMessage());
//                        }
//                    }, executorService))
//                    .collect(Collectors.toList());
//
//            // 배치 완료 대기
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//            // 배치 결과 집계
//            successCount += batch.size();
//
//            // API 호출 제한을 위한 딜레이 (배치 단위로 조정)
//            if (i < totalBatches - 1) {
//                try {
//                    Thread.sleep(1000); // 배치당 1초 딜레이 (네이버 API 제한 고려)
//                    log.info("배치 {} 완료, 다음 배치까지 1초 대기...", i + 1);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//        }
//
//        log.info("네이버 API 좌표 업데이트 완료: 성공={}, 실패={}", successCount, failCount);
//    }
//
//    @Transactional
//    public void updateCoordinate(PopularTouristSpotsEntity spot) {
//        String searchQuery = buildSearchQuery(spot);
//
//        // 개별 API 호출 전 딜레이 추가 (네이버 API 제한 고려)
//        try {
//            Thread.sleep(200); // 개별 호출당 0.2초 딜레이
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return;
//        }
//
//        try {
//            // 타임아웃 설정
//            CompletableFuture<Optional<NaverLocalSearchApiClient.NaverLocalResponse.Item>> future =
//                CompletableFuture.supplyAsync(() -> naverApiClient.searchLocation(searchQuery))
//                    .orTimeout(5, TimeUnit.SECONDS); // 5초 타임아웃
//
//            future.thenAccept(itemOpt -> {
//                itemOpt.ifPresentOrElse(
//                    item -> {
//                        // TM 좌표를 WGS84 좌표로 변환
//                        try {
//                            double[] wgs84Coords = convertTMToWGS84(
//                                Double.parseDouble(item.mapx()),
//                                Double.parseDouble(item.mapy())
//                            );
//
//                            // 좌표 설정 (변환된 WGS84 좌표 사용)
//                            spot.setLongitude(BigDecimal.valueOf(wgs84Coords[0]));
//                            spot.setLatitude(BigDecimal.valueOf(wgs84Coords[1]));
//
//                            log.debug("좌표 변환: TM({}, {}) -> WGS84({}, {})",
//                                    item.mapx(), item.mapy(), wgs84Coords[0], wgs84Coords[1]);
//
//                        } catch (Exception e) {
//                            log.error("좌표 변환 실패: TM({}, {}), error={}",
//                                    item.mapx(), item.mapy(), e.getMessage());
//                            return;
//                        }
//
//                        // 시군구명이 null인 경우 네이버 API 응답에서 추출
//                        if (spot.getSigunguName() == null) {
//                            String extractedSigungu = extractSigunguFromAddress(item.address());
//                            if (extractedSigungu != null) {
//                                spot.setSigunguName(extractedSigungu);
//                                log.debug("시군구명 추출 및 설정: {} -> {}",
//                                        item.address(), extractedSigungu);
//                            }
//                        }
//
//                        popularTouristSpotsJPARepository.save(spot);
//                        log.debug("좌표 업데이트 성공: {} -> ({}, {})",
//                                searchQuery, spot.getLongitude(), spot.getLatitude());
//                    },
//                    () -> {
//                        // 대체 검색 시도
//                        tryAlternativeSearch(spot);
//                    }
//                );
//            }).exceptionally(throwable -> {
//                log.error("좌표 업데이트 실패: id={}, name={}, error={}",
//                        spot.getId(), spot.getSpotName(), throwable.getMessage());
//                return null;
//            });
//
//        } catch (Exception e) {
//            log.error("좌표 업데이트 중 예외 발생: id={}, name={}, error={}",
//                    spot.getId(), spot.getSpotName(), e.getMessage());
//        }
//    }
//
//    private void tryAlternativeSearch(PopularTouristSpotsEntity spot) {
//        String alternativeQuery = spot.getSpotName();
//
//        try {
//            CompletableFuture<Optional<NaverLocalSearchApiClient.NaverLocalResponse.Item>> future =
//                CompletableFuture.supplyAsync(() -> naverApiClient.searchLocation(alternativeQuery))
//                    .orTimeout(5, TimeUnit.SECONDS);
//
//            future.thenAccept(itemOpt -> {
//                itemOpt.ifPresentOrElse(
//                    item -> {
//                        // TM 좌표를 WGS84 좌표로 변환 (대체 검색에서도 동일하게 적용)
//                        try {
//                            double[] wgs84Coords = convertTMToWGS84(
//                                Double.parseDouble(item.mapx()),
//                                Double.parseDouble(item.mapy())
//                            );
//
//                            // 좌표 설정 (변환된 WGS84 좌표 사용)
//                            spot.setLongitude(BigDecimal.valueOf(wgs84Coords[0]));
//                            spot.setLatitude(BigDecimal.valueOf(wgs84Coords[1]));
//
//                            log.debug("대체 검색 좌표 변환: TM({}, {}) -> WGS84({}, {})",
//                                    item.mapx(), item.mapy(), wgs84Coords[0], wgs84Coords[1]);
//
//                        } catch (Exception e) {
//                            log.error("대체 검색 좌표 변환 실패: TM({}, {}), error={}",
//                                    item.mapx(), item.mapy(), e.getMessage());
//                            return;
//                        }
//
//                        if (spot.getSigunguName() == null) {
//                            String extractedSigungu = extractSigunguFromAddress(item.address());
//                            if (extractedSigungu != null) {
//                                spot.setSigunguName(extractedSigungu);
//                            }
//                        }
//
//                        popularTouristSpotsJPARepository.save(spot);
//                        log.debug("대체 검색으로 좌표 업데이트 성공: {} -> ({}, {})",
//                                alternativeQuery, spot.getLongitude(), spot.getLatitude());
//                    },
//                    () -> log.warn("좌표 찾기 실패: {}", spot.getSpotName())
//                );
//            });
//
//        } catch (Exception e) {
//            log.error("대체 검색 실패: id={}, name={}, error={}",
//                    spot.getId(), spot.getSpotName(), e.getMessage());
//        }
//    }
//
//    private String buildSearchQuery(PopularTouristSpotsEntity spot) {
//        // "경상북도 경주시 불국사" 형태로 검색어 구성
//        StringBuilder query = new StringBuilder();
//
//        if (spot.getSidoName() != null) {
//            // 시도명을 네이버 API 주소 형식에 맞게 변환
//            String convertedSidoName = convertSidoNameForSearch(spot.getSidoName());
//            query.append(convertedSidoName).append(" ");
//        }
//        if (spot.getSigunguName() != null) {
//            query.append(spot.getSigunguName()).append(" ");
//        }
//        if (spot.getSpotName() != null) {
//            query.append(spot.getSpotName());
//        }
//
//        return query.toString().trim();
//    }
//
//    /**
//     * DB의 sido_name을 네이버 API 주소 형식에 맞게 변환
//     */
//    private String convertSidoNameForSearch(String sidoName) {
//        if (sidoName == null) {
//            return null;
//        }
//
//        // 시도명 매핑 테이블
//        switch (sidoName) {
//            case "부산광역시":
//                return "부산광역시";
//            case "대전광역시":
//                return "대전광역시";
//            case "대구광역시":
//                return "대구광역시";
//            case "울산광역시":
//                return "울산광역시";
//            case "강원도":
//                return "강원특별자치도";
//            case "인천광역시":
//                return "인천광역시";
//            case "경상남도":
//                return "경상남도";
//            case "경상북도":
//                return "경상북도";
//            case "충청남도":
//                return "충청남도";
//            case "충청북도":
//                return "충청북도";
//            case "전라남도":
//                return "전라남도";
//            case "전북특별자치도":
//                return "전북특별자치도";
//            case "제주특별자치도":
//                return "제주특별자치도";
//            case "세종특별자치시":
//                return "세종특별자치시";
//            case "서울특별시":
//                return "서울특별시";
//            case "광주광역시":
//                return "광주광역시";
//            case "경기도":
//                return "경기도";
//            default:
//                log.warn("알 수 없는 시도명: {}", sidoName);
//                return sidoName; // 변환할 수 없는 경우 원본 반환
//        }
//    }
//
//    private String extractSigunguFromAddress(String address) {
//        if (address == null || address.trim().isEmpty()) {
//            return null;
//        }
//
//        try {
//            // 주소를 공백으로 분리
//            String[] parts = address.trim().split("\\s+");
//
//            // 최소 2개 이상의 부분이 있어야 함 (시도 시군구)
//            if (parts.length >= 2) {
//                // 두 번째 부분이 시군구 (예: "강남구", "조치원읍")
//                String sigungu = parts[1];
//
//                // "구", "시", "군", "읍"으로 끝나는지 확인
//                if (sigungu.endsWith("구") || sigungu.endsWith("시") ||
//                    sigungu.endsWith("군") || sigungu.endsWith("읍")) {
//                    return sigungu;
//                }
//            }
//
//            return null;
//
//        } catch (Exception e) {
//            log.warn("주소에서 시군구명 추출 실패: address={}, error={}", address, e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * TM 좌표계를 WGS84 좌표계로 변환 (개선된 버전)
//     * 네이버 API는 TM 좌표계를 사용하므로 이를 위경도로 변환해야 함
//     */
//    private double[] convertTMToWGS84(double tmX, double tmY) {
//        try {
//            // TM 좌표계는 한국 지역에서 사용되는 투영좌표계
//            // 더 정확한 변환을 위한 개선된 공식
//
//            // TM 좌표계의 원점 (서울)
//            double originX = 200000.0;
//            double originY = 500000.0;
//
//            // 스케일 팩터 (더 정확한 값)
//            double scaleX = 0.000008;  // 경도 변환 계수
//            double scaleY = 0.000009;  // 위도 변환 계수
//
//            // WGS84 기준 서울 좌표
//            double seoulLon = 126.9784;
//            double seoulLat = 37.5665;
//
//            // 변환
//            double lon = seoulLon + (tmX - originX) * scaleX;
//            double lat = seoulLat + (tmY - originY) * scaleY;
//
//            // 좌표 범위 검증
//            if (lon < 124.0 || lon > 132.0 || lat < 33.0 || lat > 39.0) {
//                log.warn("변환된 좌표가 한국 영역을 벗어남: TM({}, {}) -> WGS84({}, {})",
//                        tmX, tmY, lon, lat);
//                // 기본값 반환
//                return new double[]{126.9784, 37.5665}; // 서울 좌표
//            }
//
//            return new double[]{lon, lat};
//
//        } catch (Exception e) {
//            log.error("좌표 변환 중 오류 발생: TM({}, {}), error={}", tmX, tmY, e.getMessage());
//            // 오류 발생 시 기본값 반환
//            return new double[]{126.9784, 37.5665}; // 서울 좌표
//        }
//    }
//
//    /**
//     * ID로 관광지를 조회하여 좌표 업데이트
//     */
//    @Transactional
//    public void updateCoordinateById(Long id) {
//        PopularTouristSpotsEntity spot = popularTouristSpotsJPARepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("ID " + id + "에 해당하는 관광지를 찾을 수 없습니다."));
//
//        log.info("ID {} 관광지 좌표 업데이트 시작 (네이버 API): {}", id, spot.getSpotName());
//        updateCoordinate(spot);
//    }
//
//    /**
//     * ID 목록으로 여러 관광지의 좌표 일괄 업데이트
//     */
//    @Transactional
//    public void updateCoordinatesByIds(List<Long> ids) {
//        log.info("{}개 관광지의 좌표 일괄 업데이트 시작 (네이버 API)", ids.size());
//
//        int successCount = 0;
//        int failCount = 0;
//
//        for (Long id : ids) {
//            try {
//                updateCoordinateById(id);
//                successCount++;
//
//                // API 호출 제한을 위한 딜레이
//                Thread.sleep(200);
//
//            } catch (Exception e) {
//                log.error("ID {} 관광지 좌표 업데이트 실패: {}", id, e.getMessage());
//                failCount++;
//            }
//        }
//
//        log.info("ID 기반 좌표 업데이트 완료 (네이버 API): 성공={}, 실패={}", successCount, failCount);
//    }
//
//    @jakarta.annotation.PreDestroy
//    public void shutdown() {
//        executorService.shutdown();
//    }
//}
