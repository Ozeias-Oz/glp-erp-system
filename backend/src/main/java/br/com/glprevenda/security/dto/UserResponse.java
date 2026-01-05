package br.com.glprevenda.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO de resposta com informações do usuário.
 * 
 * Usado em: 
 * - GET /api/users/me (ver perfil)
 * - GET /api/admin/users (listar usuários)
 * 
 * NÃO expõe a senha (segurança!)
 * 
 * @author Ozeias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Boolean active;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}