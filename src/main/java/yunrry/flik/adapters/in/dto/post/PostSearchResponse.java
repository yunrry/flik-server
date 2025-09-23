package yunrry.flik.adapters.in.dto.post;

import lombok.Builder;

import java.util.List;

@Builder
public record PostSearchResponse(
        List<ActivityDetailPostResponse> content,
        PageableInfo pageable,
        boolean hasNext,
        int numberOfElements
) {
    @Builder
    public record PageableInfo(
            int pageNumber,
            int pageSize
    ) {}
}