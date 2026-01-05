package br.com.glprevenda.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO de resposta para autenticação (login/register).
 * 
 * Contém:
 * - Token JWT (accessToken)
 * - Refresh Token (para renovar o access token)
 * - Informações do usuário
 * - Expiração do token
 * 
 * @author Ozeias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * Token JWT para autenticação nas próximas requisições
     * Enviar no header: Authorization: Bearer {accessToken}
     */
    private String accessToken;
    
    /**
     * Token para renovar o accessToken quando expirar
     */
    private String refreshToken;
    
    /**
     * Tipo do token (sempre "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * Tempo de expiração do token em segundos
     * Exemplo: 86400 = 24 horas
     */
    private Long expiresIn;
    
    /**
     * ID do usuário
     */
    private Long userId;
    
    /**
     * Username do usuário
     */
    private String username;
    
    /**
     * Email do usuário
     */
    private String email;
    
    /**
     * Nome completo do usuário
     */
    private String fullName;
    
    /**
     * Roles (permissões) do usuário
     * Exemplo: ["ROLE_ADMIN", "ROLE_VENDEDOR"]
     */
    private Set<String> roles;
    
    /**
     * Data/hora da geração do token
     */
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();
}