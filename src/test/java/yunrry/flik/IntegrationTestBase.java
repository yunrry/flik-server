package yunrry.flik;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.config.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@Import({SecurityConfig.class, TestConfig.class, OpenAIConfig.class})
@TestPropertySource(properties = {
        "management.endpoints.enabled=false",
        "FRONTEND_URL=http://localhost:5713",
        "app.frontend.url=http://localhost:5713",
        "openai.api-key=test-api-key",
        "openai.base-url=http://localhost:8081/v1",
})
public abstract class IntegrationTestBase {
}

