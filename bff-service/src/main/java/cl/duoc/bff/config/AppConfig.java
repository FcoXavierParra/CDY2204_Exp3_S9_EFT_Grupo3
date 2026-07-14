package cl.duoc.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** RestClient para que el BFF invoque al cursos-service (core). */
@Configuration
public class AppConfig {

    @Value("${app.cursos-service.url}")
    private String cursosServiceUrl;

    @Bean
    public RestClient cursosRestClient() {
        return RestClient.builder().baseUrl(cursosServiceUrl).build();
    }
}
