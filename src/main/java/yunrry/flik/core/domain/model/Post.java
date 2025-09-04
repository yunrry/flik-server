package yunrry.flik.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class Post {
    private final Long id;
    private final Long userId;
    private final PostType type;
    private final String title;
    private final String content;
    private final List<String> imageUrls;
    private final PostMetadata metadata;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Integer visitCount;


    public Post incrementVisitCount() {
        return Post.builder()
                .id(this.id)
                .userId(this.userId)
                .type(this.type)
                .title(this.title)
                .content(this.content)
                .imageUrls(this.imageUrls)
                .metadata(this.metadata)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .visitCount(this.visitCount==null? 1 : this.visitCount + 1)
                .build();
    }

    public Post updateContent(String title, String content, List<String> imageUrls) {
        return Post.builder()
                .id(this.id)
                .userId(this.userId)
                .type(this.type)
                .title(title)
                .content(content)
                .imageUrls(imageUrls)
                .metadata(this.metadata)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .visitCount(this.visitCount)
                .build();
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }


}