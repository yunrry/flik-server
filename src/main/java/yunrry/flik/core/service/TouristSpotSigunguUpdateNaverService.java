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
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class TouristSpotSigunguUpdateNaverService {
//
//    private final PopularTouristSpotsJPARepository popularTouristSpotsJPARepository;
//    private final NaverLocalSearchApiClient naverApiClient;
//
//    // 병렬 처리를 위한 스레드 풀
//    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
//
//    @Transactional
//    public void updateAllSigunguNames() {
//        List<PopularTouristSpotsEntity> spotsWithoutSigungu = popularTouristSpotsJPARepository.findBySigunguNameIsNull();
//
//        log.info("네이버 API로 시군구명이 없는 관광지 {}개 발견", spotsWithoutSigungu.size());
//
//        // 배치 크기 설정 (더 작게 조정)
//        int batchSize = 10; // 50 -> 20으로 감소
//        int totalBatches = (int) Math.ceil((double) spotsWithoutSigungu.size() / batchSize);
//
//        int successCount = 0;
//        int failCount = 0;
//
//        // 배치 단위로 병렬 처리
//        for (int i = 0; i < totalBatches; i++) {
//            int startIndex = i * batchSize;
//            int endIndex = Math.min(startIndex + batchSize, spotsWithoutSigungu.size());
//            List<PopularTouristSpotsEntity> batch = spotsWithoutSigungu.subList(startIndex, endIndex);
//
//            log.info("배치 {}/{} 처리 중... ({}-{})", i + 1, totalBatches, startIndex + 1, endIndex);
//
//            // 병렬 처리 (스레드 수 감소)
//            List<CompletableFuture<Void>> futures = batch.stream()
//                    .map(spot -> CompletableFuture.runAsync(() -> {
//                        try {
//                            updateSigunguName(spot);
//                        } catch (Exception e) {
//                            log.error("시군구명 업데이트 실패: id={}, name={}, error={}",
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
//            // API 호출 제한을 위한 딜레이 증가 (배치 단위로 조정)
//            if (i < totalBatches - 1) {
//                try {
//                    Thread.sleep(1500); // 0.2초 -> 1초로 증가
//                    log.info("배치 {} 완료, 다음 배치까지 1초 대기...", i + 1);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//        }
//
//        log.info("네이버 API 시군구명 업데이트 완료: 성공={}, 실패={}", successCount, failCount);
//    }
//
//    @Transactional
//    public void updateSigunguName(PopularTouristSpotsEntity spot) {
//        if (spot.getSigunguName() != null) {
//            log.debug("이미 시군구명이 설정되어 있음: {}", spot.getSpotName());
//            return;
//        }
//
//        String searchQuery = buildSearchQuery(spot);
//        log.info("네이버 지역검색 시도: {} -> 검색어: {}", spot.getSpotName(), searchQuery);
//
//        // 개별 API 호출 전 딜레이 추가
//        try {
//            Thread.sleep(200); // 개별 호출당 0.2초 딜레이
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return;
//        }
//
//        // 네이버 API로 지역 검색
//        naverApiClient.searchLocation(searchQuery)
//                .ifPresentOrElse(
//                        item -> {
//                            log.info("네이버 지역검색 성공: {} -> 제목: {}, 주소: {}",
//                                    searchQuery, item.title(), item.address());
//
//                            // 시도명 일치 여부 확인
//                            if (!isValidSidoMatch(spot.getSidoName(), item.address())) {
//                                log.warn("시도명 불일치: 기대={}, 실제={} -> 대체 검색 시도",
//                                        spot.getSidoName(), item.address());
//                                tryAlternativeSearch(spot);
//                                return;
//                            }
//
//                            String extractedSigungu = extractSigunguFromAddress(item.address());
//                            if (extractedSigungu != null) {
//                                spot.setSigunguName(extractedSigungu);
//                                popularTouristSpotsJPARepository.save(spot);
//
//                                log.info("시군구명 업데이트 성공: {} -> {} (주소: {})",
//                                        spot.getSpotName(), extractedSigungu, item.address());
//                            } else {
//                                log.warn("주소에서 시군구명 추출 실패: {} -> 주소: {}",
//                                        searchQuery, item.address());
//                            }
//                        },
//                        () -> {
//                            log.warn("네이버 지역검색 실패: {} -> 대체 검색 시도", searchQuery);
//                            tryAlternativeSearch(spot);
//                        }
//                );
//    }
//
//    private void tryAlternativeSearch(PopularTouristSpotsEntity spot) {
//        String alternativeQuery = spot.getSpotName();
//        log.info("대체 검색 시도: {} -> 검색어: {}", spot.getSpotName(), alternativeQuery);
//
//        naverApiClient.searchLocation(alternativeQuery)
//                .ifPresentOrElse(
//                        item -> {
//                            log.info("대체 검색 성공: {} -> 제목: {}, 주소: {}",
//                                    alternativeQuery, item.title(), item.address());
//
//                            // 대체 검색에서도 시도명 일치 여부 확인
//                            if (!isValidSidoMatch(spot.getSidoName(), item.address())) {
//                                log.warn("대체 검색에서도 시도명 불일치: 기대={}, 실제={} -> 시군구명 업데이트 건너뜀",
//                                        spot.getSidoName(), item.address());
//                                return;
//                            }
//
//                            String extractedSigungu = extractSigunguFromAddress(item.address());
//                            if (extractedSigungu != null) {
//                                spot.setSigunguName(extractedSigungu);
//                                popularTouristSpotsJPARepository.save(spot);
//
//                                log.info("대체 검색으로 시군구명 업데이트 성공: {} -> {} (주소: {})",
//                                        spot.getSpotName(), extractedSigungu, item.address());
//                            } else {
//                                log.warn("대체 검색에서도 시군구명 추출 실패: {} -> 주소: {}",
//                                        alternativeQuery, item.address());
//                            }
//                        },
//                        () -> log.warn("대체 검색도 실패: {} -> 시군구명 찾기 실패", alternativeQuery)
//                );
//    }
//
//    private String buildSearchQuery(PopularTouristSpotsEntity spot) {
//        StringBuilder query = new StringBuilder();
//
//        if (spot.getSidoName() != null) {
//            query.append(spot.getSidoName()).append(" ");
//        }
//        if (spot.getSpotName() != null) {
//            query.append(spot.getSpotName());
//        }
//
//        return query.toString().trim();
//    }
//
//    /**
//     * 시도명과 네이버 API 주소 결과를 비교하여 유효성 검증
//     * DB의 sido_name과 네이버 API 주소의 첫 번째 단어를 매핑하여 비교
//     */
//    private boolean isValidSidoMatch(String sidoName, String address) {
//        if (sidoName == null || address == null) {
//            return false;
//        }
//
//        String[] addressParts = address.trim().split("\\s+");
//        if (addressParts.length == 0) {
//            return false;
//        }
//
//        String firstAddressWord = addressParts[0];
//
//        // 디버깅을 위한 로그 추가
//        log.debug("시도명 비교: 기대={}, 실제 첫번째단어={}, 전체주소={}",
//                sidoName, firstAddressWord, address);
//
//        // 시도명 매핑 테이블
//        switch (sidoName) {
//            case "부산광역시":
//                return firstAddressWord.equals("부산광역시");
//            case "대전광역시":
//                return firstAddressWord.equals("대전광역시");
//            case "대구광역시":
//                return firstAddressWord.equals("대구광역시");
//            case "울산광역시":
//                return firstAddressWord.equals("울산광역시");
//            case "강원도":
//                return firstAddressWord.equals("강원특별자치도");
//            case "인천광역시":
//                return firstAddressWord.equals("인천광역시");
//            case "경상남도":
//                return firstAddressWord.equals("경상남도");
//            case "경상북도":
//                return firstAddressWord.equals("경상북도");
//            case "충청남도":
//                return firstAddressWord.equals("충청남도");
//            case "충청북도":
//                return firstAddressWord.equals("충청북도");
//            case "전라남도":
//                return firstAddressWord.equals("전라남도");
//            case "전북특별자치도":
//                return firstAddressWord.equals("전북특별자치도");
//            case "제주특별자치도":
//                return firstAddressWord.equals("제주특별자치도");
//            case "세종특별자치시":
//                boolean result = firstAddressWord.equals("세종특별자치시");
//                log.debug("세종특별자치시 매칭 결과: {}", result);
//                return result;
//            case "서울특별시":
//                return firstAddressWord.equals("서울특별시");
//            case "광주광역시":
//                return firstAddressWord.equals("광주광역시");
//            case "경기도":
//                return firstAddressWord.equals("경기도");
//            default:
//                log.warn("알 수 없는 시도명: {}", sidoName);
//                return false;
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
//                // 두 번째 부분이 시군구 (예: "강남구")
//                String sigungu = parts[1];
//                return sigungu;
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
//     * ID로 관광지를 조회하여 시군구명 업데이트
//     */
//    @Transactional
//    public void updateSigunguNameById(Long id) {
//        PopularTouristSpotsEntity spot = popularTouristSpotsJPARepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("ID " + id + "에 해당하는 관광지를 찾을 수 없습니다."));
//
//        log.info("ID {} 관광지 시군구명 업데이트 시작 (네이버 API): {}", id, spot.getSpotName());
//        updateSigunguName(spot);
//    }
//
//    /**
//     * ID 목록으로 여러 관광지의 시군구명 일괄 업데이트
//     */
//    @Transactional
//    public void updateSigunguNamesByIds(List<Long> ids) {
//        log.info("{}개 관광지의 시군구명 일괄 업데이트 시작 (네이버 API)", ids.size());
//
//        int successCount = 0;
//        int failCount = 0;
//
//        for (Long id : ids) {
//            try {
//                updateSigunguNameById(id);
//                successCount++;
//
//                // API 호출 제한을 위한 딜레이
//                Thread.sleep(100);
//
//            } catch (Exception e) {
//                log.error("ID {} 관광지 시군구명 업데이트 실패: {}", id, e.getMessage());
//                failCount++;
//            }
//        }
//
//        log.info("ID 기반 시군구명 업데이트 완료 (네이버 API): 성공={}, 실패={}", successCount, failCount);
//    }
//
//    @jakarta.annotation.PreDestroy
//    public void shutdown() {
//        executorService.shutdown();
//    }
//}
