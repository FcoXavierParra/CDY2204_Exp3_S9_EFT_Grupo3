package cl.duoc.cursos.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Cursos Service - Plataforma de Cursos en Linea (CDY2204 EFT)",
        version = "1.0.0",
        description = "Microservicio core: dominio de cursos y matriculas, persistencia en Oracle Cloud "
            + "y material de cursos en AWS S3. Securitizado con Spring Security + Azure AD B2C."),
    security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer",
    bearerFormat = "JWT", description = "Token JWT de Azure AD B2C. Formato: Bearer {token}")
public class OpenApiConfig {
}
