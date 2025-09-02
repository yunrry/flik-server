//package yunrry.flik.infrastructure.adapter.out.persistence;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import yys.safewalk.application.port.out.LoadPopularTouristSpotPort;
//import yys.safewalk.domain.model.PopularTouristSpots;
//import yys.safewalk.entity.PopularTouristSpotsEntity;
//
//import java.util.List;
//import java.util.Optional;
//
//@Component
//@RequiredArgsConstructor
//public class PopularTouristSpotsAdapter implements LoadPopularTouristSpotPort {
//
//    private final PopularTouristSpotsJPARepository popularTouristSpotsJPARepository;
//
//    @Override
//    public Optional<PopularTouristSpots> loadById(String id) {
//        // List로 받아서 첫 번째 결과만 반환 (중복 데이터 처리)
//        List<PopularTouristSpotsEntity> results = popularTouristSpotsJPARepository.findAllByTouristSpotId(id);
//        if (results.isEmpty()) {
//            return Optional.empty();
//        }
//
//        // 첫 번째 결과만 사용하고 도메인 모델로 변환
//        PopularTouristSpotsEntity firstResult = results.get(0);
//        return Optional.of(firstResult.toDomain());
//    }
//}