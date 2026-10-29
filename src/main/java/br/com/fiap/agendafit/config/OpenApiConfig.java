package br.com.fiap.agendafit.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Swagger UI em /swagger-ui.html; o botão Authorize recebe o token do /auth/login
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Agenda Fit API",
        version = "1.0.0",
        description = "API de agenda de academia: grade de aulas com paginação, buscas por exemplo e por filtros, "
                + "HATEOAS com links conforme o usuário logado e segurança com JWT e roles USER e ADMIN."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
