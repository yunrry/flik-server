package yunrry.flik.ports.in.usecase;

public interface SpotAnalyticsUseCase {

    /**
     * 장소의 저장 횟수 증가
     */
    void incrementSaveCount(Long spotId);

    /**
     * 장소의 총 저장 횟수 조회
     */
    int getSaveCount(Long spotId);
}