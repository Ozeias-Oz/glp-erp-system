package br.com.glprevenda.security.controller;

import br.com.glprevenda.security.dto.UserResponse;
import br.com. glprevenda.security.entity.User;
import br.com.glprevenda.security. repository.UserRepository;
import br.com.glprevenda. security.service.AuthService;
import br.com.glprevenda. shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework. security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web. bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger. v3.oas.annotations. security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para gerenciamento de usuários.
 * 
 * Endpoints: 
 * - GET /api/users/me → Ver perfil do usuário logado (autenticado)
 * - GET /api/admin/users → Listar todos os usuários (só ADMIN)
 * - GET /api/admin/users/{id} → Ver usuário específico (só ADMIN)
 * 
 * @author Ozeias
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {
    
    private final UserRepository userRepository;
    private final AuthService authService;
    
    /**
     * Ver perfil do usuário logado. 
     * 
     * GET /api/users/me
     * 
     * Header:
     * Authorization: Bearer {accessToken}
     * 
     * Retorna:
     * {
     *   "id": 1,
     *   "username": "ozeias",
     *   "email": "ozeias@email.com",
     *   "fullName":  "Ozeias Silva",
     *   "active": true,
     *   "roles": ["ROLE_VENDEDOR"],
     *   "createdAt": "2024-01-01T10:00:00",
     *   "updatedAt": "2024-01-01T10:00:00"
     * }
     * 
     * @param currentUser Usuário logado (injetado automaticamente pelo Spring Security)
     * @return Dados do usuário
     */
    
    @Operation(
    	    summary = "Ver perfil do usuário logado",
    	    description = "Retorna dados do usuário autenticado",
    	    security = @SecurityRequirement(name = "Bearer Authentication")
    	)
    	@ApiResponses({
    	    @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
    	    @ApiResponse(responseCode = "401", description = "Não autenticado")
    	})
    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("Buscando perfil do usuário:  {}", currentUser.getUsername());
        
        UserResponse response = authService.toUserResponse(currentUser);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Listar todos os usuários (só ADMIN).
     * 
     * GET /api/admin/users
     * 
     * Header:
     * Authorization: Bearer {accessToken}
     * 
     * Requer role:  ROLE_ADMIN
     * 
     * Retorna lista de usuários. 
     * 
     * @return Lista de usuários
     */
    
    @Operation(
    	    summary = "Listar todos os usuários (ADMIN)",
    	    description = "Retorna lista completa de usuários.  Requer role ROLE_ADMIN.",
    	    security = @SecurityRequirement(name = "Bearer Authentication")
    	)
    	@ApiResponses({
    	    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
    	    @ApiResponse(responseCode = "401", description = "Não autenticado"),
    	    @ApiResponse(responseCode = "403", description = "Sem permissão (não é ADMIN)")
    	})
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin listando todos os usuários");
        
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(authService::toUserResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Ver usuário específico por ID (só ADMIN).
     * 
     * GET /api/admin/users/{id}
     * 
     * Header:
     * Authorization: Bearer {accessToken}
     * 
     * Requer role: ROLE_ADMIN
     * 
     * @param id ID do usuário
     * @return Dados do usuário
     */
    
    @Operation(
    	    summary = "Ver usuário por ID (ADMIN)",
    	    description = "Retorna dados de um usuário específico.  Requer role ROLE_ADMIN.",
    	    security = @SecurityRequirement(name = "Bearer Authentication")
    	)
    	@ApiResponses({
    	    @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
    	    @ApiResponse(responseCode = "401", description = "Não autenticado"),
    	    @ApiResponse(responseCode = "403", description = "Sem permissão"),
    	    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    	})
    @GetMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Admin buscando usuário: ID={}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        
        UserResponse response = authService.toUserResponse(user);
        
        return ResponseEntity.ok(response);
    }
}