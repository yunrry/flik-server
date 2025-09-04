package yunrry.flik.adapters.in.dto;

import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.function.Function;

public record SliceSearchResponse<T>(
        List<T> content,
        PageableInfo pageable,
        boolean hasNext,
        int numberOfElements
) {
    public static <S, T> SliceSearchResponse<T> from(Slice<S> slice, Function<S, T> mapper) {
        List<T> content = slice.getContent().stream()
                .map(mapper)
                .toList();

        PageableInfo pageableInfo = new PageableInfo(
                slice.getNumber(),
                slice.getSize(),
                slice.getSort().toString()
        );

        return new SliceSearchResponse<>(
                content,
                pageableInfo,
                slice.hasNext(),
                slice.getNumberOfElements()
        );
    }

    public record PageableInfo(
            int pageNumber,
            int pageSize,
            String sort
    ) {}
}