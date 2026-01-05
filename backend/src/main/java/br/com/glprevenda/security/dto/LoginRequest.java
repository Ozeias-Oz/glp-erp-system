package br.com.glprevenda.security. dto;

import jakarta.validation. constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok. NoArgsConstructor;

/**
 * DTO para requisição de login.
 * 
 * Pode usar username OU email para fazer login.
 * 
 * @author Ozeias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username ou email é obrigatório")
    private String usernameOrEmail;
    
    @NotBlank(message = "Senha é obrigatória")
    private String password;
}
