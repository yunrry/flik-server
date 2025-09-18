//package yunrry.flik.core.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.condition.EnabledIf;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Profile;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.test.StepVerifier;
//import yunrry.flik.IntegrationTestBase;
//import yunrry.flik.core.service.embedding.OpenAIEmbeddingService;
//
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//
//@ActiveProfiles("test")
//class OpenAIEmbeddingServiceIntegrationTest {
//
//    private OpenAIEmbeddingService service;
//    @Value("${openai.api-key}")
//    private String apiKey;
//
//
//    @BeforeEach
//    void setUp() {
//
//        WebClient webClient = WebClient.builder()
//                .baseUrl("https://api.openai.com/v1")
//                .defaultHeader("Authorization", "Bearer " + apiKey)
//                .build();
//
//        service = new OpenAIEmbeddingService(webClient);
//        ReflectionTestUtils.setField(service, "embeddingModel", "text-embedding-3-small");
//    }
//
//    @Test
//    void createEmbedding_RealAPI() {
//        StepVerifier.create(service.createEmbedding("카페"))
//                .assertNext(embedding -> assertThat(embedding).hasSize(1536))
//                .verifyComplete();
//    }
//}