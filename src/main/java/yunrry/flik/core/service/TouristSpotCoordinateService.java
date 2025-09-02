//package yunrry.flik.application.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import yys.safewalk.entity.PopularTouristSpotsEntity;
//import yys.safewalk.infrastructure.adapter.out.persistence.PopularTouristSpotsJPARepository;
//import yys.safewalk.infrastructure.external.KakaoMapApiClient;
//
//
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class TouristSpotCoordinateService {
//
//    private final PopularTouristSpotsJPARepository popularTouristSpotsJPARepository;
//    private final KakaoMapApiClient kakaoMapApiClient;
//
//    @Transactional
//    public void updateAllCoordinates() {
//        List<PopularTouristSpotsEntity> spotsWithoutCoordinates = popularTouristSpotsJPARepository.findByLongitudeIsNullOrLatitudeIsNull();
//
//        log.info("좌표가 없는 관광지 {}개 발견", spotsWithoutCoordinates.size());
//
//        // 1. 배치 크기 설정
//        int batchSize = 100;
//        int totalBatches = (int) Math.ceil((double) spotsWithoutCoordinates.size() / batchSize);
//
//        int successCount = 0;
//        int failCount = 0;
//
//        // 2. 배치 단위로 처리
//        for (int i = 0; i < totalBatches; i++) {
//            int startIndex = i * batchSize;
//            int endIndex = Math.min(startIndex + batchSize, spotsWithoutCoordinates.size());
//            List<PopularTouristSpotsEntity> batch = spotsWithoutCoordinates.subList(startIndex, endIndex);
//
//            log.info("배치 {}/{} 처리 중... ({}-{})", i + 1, totalBatches, startIndex + 1, endIndex);
//
//            // 3. 병렬 처리 (CompletableFuture 사용)
//            List<CompletableFuture<Void>> futures = batch.stream()
//                    .map(spot -> CompletableFuture.runAsync(() -> {
//                        try {
//                            updateCoordinate(spot);
//                        } catch (Exception e) {
//                            log.error("좌표 업데이트 실패: id={}, name={}, error={}",
//                                    spot.getId(), spot.getSpotName(), e.getMessage());
//                        }
//                    }))
//                    .collect(Collectors.toList());
//
//            // 4. 배치 완료 대기
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//            // 5. 배치 결과 집계
//            successCount += batch.size();
//
//            // 6. API 호출 제한을 위한 딜레이 (배치 단위로 조정)
//            if (i < totalBatches - 1) { // 마지막 배치가 아닌 경우에만 딜레이
//                try {
//                    Thread.sleep(500); // 배치당 0.5초 딜레이
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//        }
//
//        log.info("좌표 업데이트 완료: 성공={}, 실패={}", successCount, failCount);
//    }
//
//    @Transactional
//    public void updateCoordinate(PopularTouristSpotsEntity spot) {
//        String searchQuery = buildSearchQuery(spot);
//
//        try {
//            // 7. 타임아웃 설정
//            CompletableFuture<Optional<KakaoMapApiClient.KakaoLocationResponse.Document>> future =
//                CompletableFuture.supplyAsync(() -> kakaoMapApiClient.searchLocation(searchQuery))
//                    .orTimeout(3, TimeUnit.SECONDS); // 3초 타임아웃
//
//            future.thenAccept(documentOpt -> {
//                documentOpt.ifPresentOrElse(
//                    document -> {
//                        spot.setLongitude(document.getLongitude());
//                        spot.setLatitude(document.getLatitude());
//
//                        // 시군구명이 null인 경우 카카오 API 응답에서 추출
//                        if (spot.getSigunguName() == null) {
//                            String extractedSigungu = extractSigunguFromAddress(document.getAddressName());
//                            if (extractedSigungu != null) {
//                                spot.setSigunguName(extractedSigungu);
//                                log.debug("시군구명 추출 및 설정: {} -> {}",
//                                        document.getAddressName(), extractedSigungu);
//                            }
//                        }
//
//                        popularTouristSpotsJPARepository.save(spot);
//                        log.debug("좌표 업데이트 성공: {} -> ({}, {})",
//                                searchQuery, document.getLongitude(), document.getLatitude());
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
//            CompletableFuture<Optional<KakaoMapApiClient.KakaoLocationResponse.Document>> future =
//                CompletableFuture.supplyAsync(() -> kakaoMapApiClient.searchLocation(alternativeQuery))
//                    .orTimeout(3, TimeUnit.SECONDS);
//
//            future.thenAccept(documentOpt -> {
//                documentOpt.ifPresentOrElse(
//                    document -> {
//                        spot.setLongitude(document.getLongitude());
//                        spot.setLatitude(document.getLatitude());
//
//                        if (spot.getSigunguName() == null) {
//                            String extractedSigungu = extractSigunguFromAddress(document.getAddressName());
//                            if (extractedSigungu != null) {
//                                spot.setSigunguName(extractedSigungu);
//                            }
//                        }
//
//                        popularTouristSpotsJPARepository.save(spot);
//                        log.debug("대체 검색으로 좌표 업데이트 성공: {} -> ({}, {})",
//                                alternativeQuery, document.getLongitude(), document.getLatitude());
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
//            // 시도명을 카카오 API 주소 형식에 맞게 변환
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
//     * DB의 sido_name을 카카오 API 주소 형식에 맞게 변환
//     */
//    private String convertSidoNameForSearch(String sidoName) {
//        if (sidoName == null) {
//            return null;
//        }
//
//        // 시도명 매핑 테이블 (TouristSpotSigunguUpdateService와 동일)
//        switch (sidoName) {
//            case "부산광역시":
//                return "부산";
//            case "대전광역시":
//                return "대전";
//            case "대구광역시":
//                return "대구";
//            case "울산광역시":
//                return "울산";
//            case "강원도":
//                return "강원특별자치도";
//            case "인천광역시":
//                return "인천";
//            case "경상남도":
//                return "경남";
//            case "경상북도":
//                return "경북";
//            case "충청남도":
//                return "충남";
//            case "충청북도":
//                return "충북";
//            case "전라남도":
//                return "전남";
//            case "전북특별자치도":
//                return "전북특별자치도";
//            case "제주특별자치도":
//                return "제주특별자치도";
//            case "세종특별자치시":
//                return "세종특별자치시";
//            case "서울특별시":
//                return "서울";
//            case "광주광역시":
//                return "광주";
//            case "경기도":
//                return "경기";
//            default:
//                log.warn("알 수 없는 시도명: {}", sidoName);
//                return sidoName; // 변환할 수 없는 경우 원본 반환
//        }
//    }
//
//    private String extractSigunguFromAddress(String address) {
//        if (address == null) {
//            return null;
//        }
//        String[] parts = address.split(" ");
//        for (int i = 0; i < parts.length - 1; i++) {
//            if (parts[i].equals("시") || parts[i].equals("군") || parts[i].equals("구")) {
//                return parts[i + 1];
//            }
//        }
//        return null;
//    }
//}