package cl.duoc.bff.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "BFF Service - Plataforma de Cursos en Linea (CDY2204 EFT)", version = "1.0.0",
        description = "Backend for Frontend: expone la API al frontend y orquesta las colas RabbitMQ "
            + "(endpoints publicar/consumir). Securitizado con Azure AD B2C."),
    security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer",
    bearerFormat = "JWT", description = "Token JWT de Azure AD B2C. Formato: Bearer {token}")
public class OpenApiConfig {
}
