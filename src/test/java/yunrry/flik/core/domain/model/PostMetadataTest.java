//package yunrry.flik.core.domain.model;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DisplayName("게시물 메타데이터 테스트")
//class PostMetadataTest {
//
//    @Test
//    @DisplayName("메타데이터가 정상적으로 생성된다")
//    void shouldCreateMetadata() {
//        // given & when
//        PostMetadata metadata = PostMetadata.of(
//                "마리오네",
//                "성수동",
//                BigDecimal.valueOf(4.5)
//        );
//
//        // then
//        assertThat(metadata.getSpotName()).isEqualTo("마리오네");
//        assertThat(metadata.getLocation()).isEqualTo("성수동");
//        assertThat(metadata.getRating()).isEqualTo(BigDecimal.valueOf(4.5));
//    }
//
//    @Test
//    @DisplayName("빈 메타데이터가 생성된다")
//    void shouldCreateEmptyMetadata() {
//        // when
//        PostMetadata metadata = PostMetadata.empty();
//
//        // then
//        assertThat(metadata.getSpotName()).isNull();
//        assertThat(metadata.getLocation()).isNull();
//        assertThat(metadata.getRating()).isNull();
//    }
//}