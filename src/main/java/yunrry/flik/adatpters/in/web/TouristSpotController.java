//package yunrry.flik.infrastructure.adapter.in.web;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.hibernate.annotations.Parameter;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import yunrry.flik.application.port.in.GetTouristSpotAccidentsQuery;
//import yunrry.flik.application.port.in.dto.EmdResponse;
//import yunrry.flik.application.port.in.dto.TouristSpotResponse;
//import yunrry.flik.application.port.in.dto.TouristSpotSearchResponse;
//import yunrry.flik.application.port.in.dto.TouristSpotsInStateResponse;
//import yunrry.flik.application.service.TouristSpotAreaService;
//import yunrry.flik.application.service.TouristSpotSearchService;
//import yunrry.flik.application.service.TouristSpotsInStateService;
//import yunrry.flik.domain.model.TouristSpotAccidentResponse;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Slf4j
//@RestController
//@RequestMapping("/v1")
//@RequiredArgsConstructor
//@Tag(name = "관광지 API", description = "관광지 조회 관련 API")
//public class TouristSpotController {
//
//    private final TouristSpotAreaService touristSpotAreaService;
//    private final TouristSpotsInStateService touristSpotsInStateService;
//    private final GetTouristSpotAccidentsQuery getTouristSpotAccidentsQuery;
//    private final TouristSpotSearchService touristSpotSearchService;
//
//    @GetMapping("/tourist-spots")
//    @Operation(
//            summary = "지도 영역 내 관광지 조회",
//            description = "지정된 지도 영역(남서쪽 좌표와 북동쪽 좌표로 정의)에 포함되는 관광지 목록을 조회합니다.",
//            parameters = {
//                    @Parameter(name = "swLat", description = "남서쪽 위도", example = "35.820"),
//                    @Parameter(name = "swLng", description = "남서쪽 경도", example = "129.200"),
//                    @Parameter(name = "neLat", description = "북동쪽 위도", example = "35.850"),
//                    @Parameter(name = "neLng", description = "북동쪽 경도", example = "129.230")
//            }
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "관광지 조회 성공",
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(implementation = TouristSpotResponse.class)
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "잘못된 요청 (좌표 유효성 검증 실패)",
//                    content = @Content(mediaType = "application/json")
//            ),
//            @ApiResponse(
//                    responseCode = "500",
//                    description = "서버 내부 오류",
//                    content = @Content(mediaType = "application/json")
//            )
//    })
//    public ResponseEntity<List<TouristSpotResponse>> getTouristSpotsByArea(
//            @RequestParam BigDecimal swLat,
//            @RequestParam BigDecimal swLng,
//            @RequestParam BigDecimal neLat,
//            @RequestParam BigDecimal neLng
//    ) {
//        Coordinate swCoordinate = new Coordinate(swLat, swLng);
//        Coordinate neCoordinate = new Coordinate(neLat, neLng);
//
//        List<TouristSpotResponse> response = touristSpotAreaService.getTouristSpotsByArea(swCoordinate, neCoordinate);
//        return ResponseEntity.ok(response);
//    }
//
//
//    @GetMapping("/tourist-spots/state")
//    @Operation(
//            summary = "특정 지역 관광지 조회",
//            description = "도내 관광지 목록을 조회합니다.",
//            parameters = {
//                    @Parameter(name = "code", description = "도시코드", example = "52"),
//                    @Parameter(name = "mode", description = "인기관광지/중심관광지/지역맛집", example = "인기관광지"),
//            }
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "관광지 조회 성공",
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(implementation = TouristSpotResponse.class)
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "잘못된 요청 (좌표 유효성 검증 실패)",
//                    content = @Content(mediaType = "application/json")
//            ),
//            @ApiResponse(
//                    responseCode = "500",
//                    description = "서버 내부 오류",
//                    content = @Content(mediaType = "application/json")
//            )
//    })
//    public ResponseEntity<List<TouristSpotsInStateResponse>> getTouristSpotsByArea(
//            @RequestParam String code,
//            @RequestParam String mode
//    ) {
//
//        List<TouristSpotsInStateResponse> response = touristSpotsInStateService.getTouristSpotsInState(code, mode);
//        return ResponseEntity.ok(response);
//    }
//
//
//
//    @GetMapping("/tourist-spots/{spotId}")
//    @Operation(
//            summary = "관광지 반경 사고 조회",
//            description = "관광지 반경 사고 목록을 조회합니다.",
//            parameters = {
//                    @Parameter(name = "spotId", description = "관광지ID", example = "4188964d50de8143b3ea67e371d64678"),
//                    @Parameter(name = "km", description = "반경 크기", example = "5"),
//            }
//    )
//    public ResponseEntity<TouristSpotAccidentResponse> getAccidentsInRadius(
//            @PathVariable String spotId,
//            @RequestParam(defaultValue = "5") Integer km) {
//
//        log.info("관광지 반경 내 교통사고 조회 요청: spotId={}, radius={}km", spotId, km);
//
//        try {
//            TouristSpotAccidentResponse response = getTouristSpotAccidentsQuery.getAccidentsInRadius(spotId, km);
//            return ResponseEntity.ok(response);
//        } catch (IllegalArgumentException e) {
//            log.warn("잘못된 요청: {}", e.getMessage());
//            return ResponseEntity.badRequest().build();
//        } catch (Exception e) {
//            log.error("교통사고 조회 중 오류 발생", e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//
//    @GetMapping("/tourist-spots/search/realtime")
//    @Operation(
//            summary = "관광지 실시간 검색",
//            description = "자동완성을 위한 실시간 관광지 검색을 제공합니다.",
//            parameters = {
//                    @Parameter(name = "query", description = "검색할 관광지명", example = "불국"),
//                    @Parameter(name = "limit", description = "반환할 최대 결과 수", example = "10")
//            }
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "실시간 검색 성공",
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(implementation = EmdResponse.class)
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "잘못된 요청 (검색어 유효성 검증 실패)",
//                    content = @Content(mediaType = "application/json")
//            ),
//            @ApiResponse(
//                    responseCode = "500",
//                    description = "서버 내부 오류",
//                    content = @Content(mediaType = "application/json")
//            )
//    })
//    public ResponseEntity<List<TouristSpotSearchResponse>> searchRealtime(
//            @RequestParam String query,
//            @RequestParam(defaultValue = "10") int limit
//    ) {
//        List<TouristSpotSearchResponse> response = touristSpotSearchService.searchRealtime(query, limit);
//        return ResponseEntity.ok(response);
//    }
//}