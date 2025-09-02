// SwaggerConfig.java
package yunrry.flik.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SafeWalk API")
                        .description("관광 안전 지도 서비스 API")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("SafeWalk Team")
                                .email("contact@safewalk.com")))
                .components(new Components()
                        .addSecuritySchemes("noauth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")))
                .servers(List.of(
                        new Server().url("https://flikapp.org/api").description("Production"),
                        new Server().url("http://localhost:8080/api").description("Local")
                ));
    }
}