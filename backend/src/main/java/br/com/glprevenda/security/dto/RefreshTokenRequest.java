package br.com.glprevenda. security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de renovação de token JWT
 * 
 * Utilizado no endpoint POST /api/auth/refresh para
 * renovar access token usando refresh token válido. 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    
    /**
     * Refresh token JWT recebido no login/registro
     */
    @NotBlank(message = "Refresh token é obrigatório")
    private String refreshToken;
}