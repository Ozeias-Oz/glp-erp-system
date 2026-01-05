package br.com.glprevenda.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de registro de novo usuário. 
 * 
 * Validações:
 * - Username: obrigatório, 3-50 caracteres
 * - Email: obrigatório, formato válido
 * - Password: obrigatório, mínimo 8 caracteres
 * - FullName: obrigatório
 * 
 * @author Ozeias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String password;
    
    @NotBlank(message = "Nome completo é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String fullName;
}
