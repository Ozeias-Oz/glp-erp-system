package br.com.glprevenda.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do SpringDoc OpenAPI (Swagger).
 * 
 * Documentação interativa da API disponível em: 
 * http://localhost:8080/swagger-ui.html
 * 
 * JSON da API (OpenAPI 3.0):
 * http://localhost:8080/v3/api-docs
 * 
 * @author Ozeias
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GLP ERP - API de Revenda de Gás",
                version = "1.0.0",
                description = """
                        Sistema de gestão para revendas de Gás GLP (botijões).
                        
                        **Funcionalidades:**
                        - Autenticação JWT (login/register)
                        - Gestão de usuários e permissões (RBAC)
                        - Controle de estoque de botijões
                        - Vendas e entregas
                        - Relatórios financeiros
                        
                        **Como usar:**
                        1. Registre-se em `/api/auth/register`
                        2. Faça login em `/api/auth/login`
                        3. Copie o `accessToken` da resposta
                        4. Clique em "Authorize" (cadeado) e cole o token
                        5. Agora pode testar os endpoints protegidos!
                        """,
                contact = @Contact(
                        name = "Ozeias",
                        email = "ozeias@glprevenda.com. br",
                        url = "https://github.com/Ozeias-Oz"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Servidor de Desenvolvimento"
                ),
                @Server(
                        url = "http://localhost:8081",
                        description = "Servidor de Homologação"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = """
                Autenticação via JWT (JSON Web Token).
                
                **Como obter o token:**
                1. Faça login em `/api/auth/login`
                2. Copie o campo `accessToken` da resposta
                3. Cole aqui (SEM o prefixo "Bearer ")
                
                **Exemplo de token:**
                eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... 
                """
)
public class OpenApiConfig {
    // Configuração feita via anotações
}