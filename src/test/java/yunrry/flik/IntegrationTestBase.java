package yunrry.flik;


import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import yunrry.flik.config.SecurityConfig;
import yunrry.flik.config.TestCacheConfig;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@Import({SecurityConfig.class, TestCacheConfig.class})
@TestPropertySource(properties = {
        "management.endpoints.enabled=false",
        "FRONTEND_URL=http://localhost:5713",
        "app.frontend.url=http://localhost:5713"
})
public abstract class IntegrationTestBase {
}