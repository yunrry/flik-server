package yunrry.flik.core.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostType {
    SAVE("save", "저장"),
    REVIEW("review", "리뷰"),
    VISIT("visit", "방문"),
    LIKE("like", "좋아요"),
    SHARE("share", "공유");

    private final String code;
    private final String description;

    public static PostType fromCode(String code) {
        for (PostType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 포스트 타입입니다: " + code);
    }
}