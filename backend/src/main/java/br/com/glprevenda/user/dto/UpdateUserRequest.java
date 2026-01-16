package br.com.glprevenda. user.dto;

import jakarta. validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de dados do usuário
 * Todos os campos são opcionais (apenas atualiza o que for enviado)
 * 
 * @author Ozeias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Email(message = "Email inválido")
    private String email;
    
    @Size(min = 3, max = 100, message = "Nome completo deve ter entre 3 e 100 caracteres")
    private String fullName;
}