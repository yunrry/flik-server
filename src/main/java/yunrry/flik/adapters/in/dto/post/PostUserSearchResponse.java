package yunrry.flik.adapters.in.dto.post;

import org.springframework.data.domain.Slice;
import yunrry.flik.core.domain.model.Post;

import java.util.List;

public record PostUserSearchResponse(
        List<UserActivityPostResponse> content,
        PageableInfo pageable,
        boolean hasNext,
        int numberOfElements
) {
    public static PostUserSearchResponse from(Slice<Post> slice) {
        List<UserActivityPostResponse> content = slice.getContent().stream()
                .map(UserActivityPostResponse::from)
                .toList();

        PageableInfo pageableInfo = new PageableInfo(
                slice.getNumber(),
                slice.getSize()
        );

        return new PostUserSearchResponse(
                content,
                pageableInfo,
                slice.hasNext(),
                slice.getNumberOfElements()
        );
    }

    public record PageableInfo(
            int pageNumber,
            int pageSize
    ) {}
}