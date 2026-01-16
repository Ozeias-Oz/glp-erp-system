package br.com.glprevenda.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO para atualização de roles do usuário
 * 
 * @author Ozeias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRolesRequest {
    
    @NotEmpty(message = "Pelo menos uma role deve ser informada")
    private Set<String> roles;
}