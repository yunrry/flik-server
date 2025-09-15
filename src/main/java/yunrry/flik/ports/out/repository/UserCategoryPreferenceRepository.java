package yunrry.flik.ports.out.repository;


public interface UserCategoryPreferenceRepository {

    /**
     * 사용자의 카테고리 선호도 점수 증가
     */
    void incrementPreferenceScore(Long userId, String detailCategory, Double increment);

    /**
     * 사용자의 카테고리 선호도 점수 조회
     */
    double getPreferenceScore(Long userId, String categoryCode);
}