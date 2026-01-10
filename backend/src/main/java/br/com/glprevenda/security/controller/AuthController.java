package br.com.glprevenda.security.controller;

import br.com.glprevenda.security.dto.AuthResponse;
import br.com.glprevenda.security.dto. LoginRequest;
import br.com.glprevenda.security. dto.RegisterRequest;
import br.com.glprevenda. security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework. http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses. ApiResponses;

import org.springframework.security.authentication.BadCredentialsException;
import br.com.glprevenda.security.dto.RefreshTokenRequest;

/**
 * Controller para autenticação (login/register).
 * 
 * Endpoints públicos (não precisa autenticação):
 * - POST /api/auth/register → Registrar novo usuário
 * - POST /api/auth/login → Fazer login
 * - POST /api/auth/refresh → Renovar token
 * 
 * @author Ozeias
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para login, registro e refresh token")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Registrar novo usuário. 
     * 
     * POST /api/auth/register
     * 
     * Body (JSON):
     * {
     *   "username": "ozeias",
     *   "email": "ozeias@email.com",
     *   "password": "senha123",
     *   "fullName": "Ozeias Silva"
     * }
     * 
     * Retorna: 
     * {
     *   "accessToken": "eyJhbGc...",
     *   "refreshToken": "eyJhbGc...",
     *   "userId": 1,
     *   "username": "ozeias",
     *   "email": "ozeias@email.com",
     *   "fullName":  "Ozeias Silva",
     *   "roles": ["ROLE_VENDEDOR"]
     * }
     * 
     * @param request Dados do novo usuário
     * @return AuthResponse com token e dados do usuário
     */
   
    @Operation(
    	    summary = "Registrar novo usuário",
    	    description = "Cria uma nova conta de usuário com role ROLE_VENDEDOR por padrão"
    	)
    	@ApiResponses({
    	    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
    	    @ApiResponse(responseCode = "400", description = "Dados inválidos (username/email já existe)")
    	})
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Requisição de registro:    username={}, email={}", 
                request.getUsername(), request.getEmail());
        
        AuthResponse response = authService.register(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Fazer login.
     * 
     * POST /api/auth/login
     * 
     * Body (JSON):
     * {
     *   "usernameOrEmail": "ozeias",  // Pode ser username OU email
     *   "password": "senha123"
     * }
     * 
     * Retorna:
     * {
     *   "accessToken":  "eyJhbGc.. .",
     *   "refreshToken": "eyJhbGc...",
     *   "userId": 1,
     *   "username": "ozeias",
     *   "roles": ["ROLE_VENDEDOR"]
     * }
     * 
     * @param request Credenciais de login
     * @return AuthResponse com token e dados do usuário
     */
    
    @Operation(
    	    summary = "Fazer login",
    	    description = "Autentica usuário e retorna token JWT.  Aceita username OU email."
    	)
    	@ApiResponses({
    	    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
    	    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    	})
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentativa de login:  {}", request.getUsernameOrEmail());
        
        AuthResponse response = authService. login(request);
        
        return ResponseEntity.ok(response);
    }
    
    
    /**
     * Renova tokens JWT usando Refresh Token
     * 
     * @param request DTO com refresh token
     * @return Novos access token e refresh token
     */
    @Operation(
            summary = "Renovar tokens",
            description = "Renova access token e refresh token usando um refresh token válido"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens renovados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido, expirado ou usuário inativo",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
                @Valid @RequestBody RefreshTokenRequest request) {
        
        log.info("Requisição de refresh token recebida");
        
        try {
            AuthResponse response = authService.refreshToken(request);
            log.info("Tokens renovados com sucesso para usuário: {}", response.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            log.error("Falha ao renovar tokens: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}