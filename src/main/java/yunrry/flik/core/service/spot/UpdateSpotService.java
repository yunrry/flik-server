package yunrry.flik.core.service.spot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunrry.flik.core.domain.model.card.Spot;
import yunrry.flik.ports.in.usecase.UpdateSpotUseCase;
import yunrry.flik.ports.out.repository.SpotRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateSpotService implements UpdateSpotUseCase {

    private final SpotRepository spotRepository;

    public Mono<Spot> findById(Long spotId) {
        return spotRepository.findByIdAsync(spotId);
    }

    @Transactional
    public Mono<Void> updateSpotTags(Long spotId, List<String> keywords) {
        return Mono.fromCallable(() -> {
                    Spot spot = spotRepository.findById(spotId);

                    String tag1 = keywords.size() > 0 ? keywords.get(0) : null;
                    String tag2 = keywords.size() > 1 ? keywords.get(1) : null;
                    String tag3 = keywords.size() > 2 ? keywords.get(2) : null;
                    String tags = keywords.size() > 3 ?
                            String.join(",", keywords.subList(3, keywords.size())) : null;

                    // Spot 도메인 객체 업데이트 (새 인스턴스 생성)
                    Spot updatedSpot = spot.withTags(tag1, tag2, tag3, tags);
                    spotRepository.save(updatedSpot);

                    log.info("Updated tags for spot {}: {}", spotId, keywords);
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}