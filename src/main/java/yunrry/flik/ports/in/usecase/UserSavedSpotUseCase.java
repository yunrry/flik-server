package yunrry.flik.ports.in.usecase;

public interface UserSavedSpotUseCase {
    /**
     * 사용자가 이미 저장한 장소인지 확인
     */
    boolean isAlreadySaved(Long userId, Long spotId);

    /**
     * 사용자의 저장 장소에 추가
     */
    void saveUserSpot(Long userId, Long spotId);

    /**
     * 저장된 장소 삭제
     */
    void removeUserSpot(Long userId, Long spotId);
}
