package br.com.glprevenda.user.controller;

import br.com.glprevenda.user.dto.*;
import br.com.glprevenda.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses. ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain. Page;
import org.springframework. data.domain.PageRequest;
import org.springframework. data.domain.Pageable;
import org.springframework.data. domain.Sort;
import org. springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework. security.access.prepost.PreAuthorize;
import org. springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gerenciamento de usuários
 * 
 * Endpoints protegidos (precisa autenticação JWT)
 * Alguns endpoints requerem role ADMIN
 * 
 * @author Ozeias
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
@SecurityRequirement(name = "bearer-auth")
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users
     * Lista todos os usuários (apenas ADMIN)
     * 
     * Query params:
     * - page: número da página (default: 0)
     * - size: tamanho da página (default: 10)
     * - sort: campo para ordenar (default: id)
     * - direction: ASC ou DESC (default: ASC)
     */
    @Operation(
            summary = "Listar usuários",
            description = "Lista todos os usuários com paginação.  Apenas ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer role ADMIN")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/users - page={}, size={}, sort={}, direction={}", 
                page, size, sort, direction);

        Sort. Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<UserResponse> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/active
     * Lista apenas usuários ativos (apenas ADMIN)
     */
    @Operation(
            summary = "Listar usuários ativos",
            description = "Lista apenas usuários ativos com paginação. Apenas ADMIN."
    )
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/users/active - page={}, size={}", page, size);

        Pageable pageable = PageRequest. of(page, size, Sort.by("username"));
        Page<UserResponse> users = userService.getActiveUsers(pageable);

        return ResponseEntity. ok(users);
    }

    /**
     * GET /api/users/search? email={email}
     * Busca usuários por email (apenas ADMIN)
     */
    @Operation(
            summary = "Buscar usuários por email",
            description = "Busca usuários por email (case-insensitive). Apenas ADMIN."
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> searchUsersByEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/users/search? email={}", email);

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.searchUsersByEmail(email, pageable);

        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/me
     * Retorna dados do usuário logado (qualquer usuário autenticado)
     */
    @Operation(
            summary = "Buscar dados do usuário logado",
            description = "Retorna informações do usuário autenticado"
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.info("GET /api/users/me - user={}", authentication.getName());

        UserResponse user = userService. getUserByUsername(authentication.getName());

        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/users/{id}
     * Busca usuário por ID (ADMIN ou próprio usuário)
     */
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna dados de um usuário específico.  ADMIN pode buscar qualquer usuário, usuário comum apenas seus próprios dados."
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("GET /api/users/{} - requestedBy={}", id, authentication.getName());

        UserResponse user = userService.getUserById(id);

        // Validar se é ADMIN ou está buscando próprios dados
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (! isAdmin && !user.getUsername().equals(authentication. getName())) {
            log.warn("Acesso negado:  usuário {} tentou acessar dados do usuário ID {}", 
                    authentication. getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(user);
    }

    /**
     * POST /api/users
     * Cria novo usuário (apenas ADMIN)
     */
    @Operation(
            summary = "Criar novo usuário",
            description = "Cria um novo usuário no sistema. Apenas ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou username/email já existe"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer role ADMIN")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Criando novo usuário: {} - requestedBy: [verificar no controller]", request.getUsername());
        

        UserResponse user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * PUT /api/users/{id}
     * Atualiza dados do usuário (ADMIN ou próprio usuário)
     */
    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza dados de um usuário.  ADMIN pode atualizar qualquer usuário, usuário comum apenas seus próprios dados."
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        log.info("PUT /api/users/{} - requestedBy={}", id, authentication.getName());

        // Validar permissão
        UserResponse existingUser = userService.getUserById(id);
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin && !existingUser. getUsername().equals(authentication.getName())) {
            log.warn("Acesso negado: usuário {} tentou atualizar usuário ID {}", 
                    authentication.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserResponse updatedUser = userService.updateUser(id, request);

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * PATCH /api/users/{id}/password
     * Altera senha do usuário (ADMIN ou próprio usuário)
     */
    @Operation(
            summary = "Alterar senha",
            description = "Altera a senha de um usuário.  Usuário comum precisa informar senha atual.  ADMIN pode alterar sem senha atual."
    )
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        log.info("PATCH /api/users/{}/password - requestedBy={}", id, authentication.getName());

        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        userService.changePassword(id, request, authentication.getName(), isAdmin);

        return ResponseEntity. noContent().build();
    }

    /**
     * PATCH /api/users/{id}/status
     * Ativa ou desativa usuário (apenas ADMIN)
     */
    @Operation(
            summary = "Alterar status do usuário",
            description = "Ativa ou desativa um usuário. Apenas ADMIN."
    )
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        log.info("PATCH /api/users/{}/status? active={}", id, active);

        UserResponse user = userService.updateUserStatus(id, active);

        return ResponseEntity.ok(user);
    }

    /**
     * PATCH /api/users/{id}/roles
     * Gerencia roles do usuário (apenas ADMIN)
     */
    @Operation(
            summary = "Atualizar roles do usuário",
            description = "Atualiza as roles (permissões) de um usuário. Apenas ADMIN."
    )
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolesRequest request) {

        log.info("PATCH /api/users/{}/roles - newRoles={}", id, request. getRoles());

        UserResponse user = userService.updateUserRoles(id, request);

        return ResponseEntity.ok(user);
    }

    /**
     * DELETE /api/users/{id}
     * Desativa usuário (soft delete) (apenas ADMIN)
     */
    @Operation(
            summary = "Desativar usuário",
            description = "Desativa um usuário (soft delete - não remove do banco). Apenas ADMIN."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{}", id);

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}