package br.com.glprevenda.security.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util. Set;
import java.util.stream.Collectors;

import org. springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework. security.authentication.UsernamePasswordAuthenticationToken;
import org. springframework.security.core.Authentication;
import org.springframework.security. core. GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.glprevenda.security.dto.AuthResponse;
import br.com.glprevenda.security.dto. LoginRequest;
import br.com.glprevenda. security.dto.RefreshTokenRequest;
import br.com.glprevenda.security. dto.RegisterRequest;
import br.com. glprevenda.security.dto.UserResponse;
import br.com.glprevenda.security.entity. Role;
import br.com.glprevenda.security.entity. User;
import br.com. glprevenda.security.repository.RoleRepository;
import br.com.glprevenda. security.repository.UserRepository;
import br.com.glprevenda. shared.exception.ResourceNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;

/**
 * Serviço de autenticação e registro de usuários.
 * 
 * Responsável por:
 * - Registrar novos usuários
 * - Fazer login (autenticação)
 * - Gerar tokens JWT
 * - Criptografar senhas
 * 
 * @author Ozeias
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Registra um novo usuário no sistema.
     * 
     * Passos:
     * 1. Valida se username/email já existem
     * 2. Criptografa a senha (BCrypt)
     * 3. Atribui role padrão (ROLE_VENDEDOR)
     * 4. Salva no banco
     * 5. Gera token JWT
     * 6. Retorna dados do usuário + token
     * 
     * @param request Dados do novo usuário
     * @return AuthResponse com token e dados do usuário
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando novo usuário: {}", request.getUsername());
        
        // Validar se já existe
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username já está em uso");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já está em uso");
        }
        
        // Buscar role padrão (ROLE_VENDEDOR)
        Role defaultRole = roleRepository.findByName("ROLE_VENDEDOR")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "ROLE_VENDEDOR"));
        
        // Criar usuário
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Criptografa senha
                .fullName(request.getFullName())
                .active(true)
                .roles(new HashSet<>())
                .build();
        
        // Adicionar role padrão
        user.addRole(defaultRole);
        
        // Salvar no banco
        user = userRepository.save(user);
        
        log.info("Usuário registrado com sucesso: ID={}, Username={}", 
                user.getId(), user.getUsername());
        
        // Gerar tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // Retornar resposta
        return buildAuthResponse(user, accessToken, refreshToken);
    }
    
    /**
     * Autentica um usuário (login).
     * 
     * Passos:
     * 1. Valida credenciais (username/email + senha)
     * 2. Se válido, carrega usuário do banco
     * 3. Gera tokens JWT
     * 4. Retorna dados do usuário + tokens
     * 
     * @param request Credenciais de login
     * @return AuthResponse com token e dados do usuário
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Tentativa de login:   {}", request.getUsernameOrEmail());
        
        // Autenticar (Spring Security valida a senha automaticamente)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );
        
        // Usuário autenticado
        User user = (User) authentication.getPrincipal();
        
        log.info("Login bem-sucedido:   ID={}, Username={}", 
                user.getId(), user.getUsername());
        
        // Gerar tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // Retornar resposta
        return buildAuthResponse(user, accessToken, refreshToken);
    }
    
    /**
     * Constrói a resposta de autenticação (login/register).
     * 
     * @param user Usuário autenticado
     * @param accessToken Token JWT
     * @param refreshToken Refresh token
     * @return AuthResponse completo
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        Set<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationInSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
    }
    
    /**
     * Converte User entity para UserResponse DTO.
     * 
     * @param user Entidade do usuário
     * @return DTO de resposta (sem senha!)
     */
    public UserResponse toUserResponse(User user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors. toSet());
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .active(user.getActive())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    /**
     * Renova tokens JWT usando Refresh Token
     * 
     * @param request DTO com refresh token
     * @return Novos tokens (access + refresh)
     * @throws BadCredentialsException se refresh token inválido
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Requisição de refresh token");
        
        try {
            // 1. Extrair username do refresh token
            String username = jwtService.extractUsername(request.getRefreshToken());
            
            if (username == null) {
                log.error("Refresh token inválido:  username não encontrado");
                throw new BadCredentialsException("Refresh token inválido");
            }
            
            // 2. Buscar usuário no banco
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("Usuário não encontrado: {}", username);
                        return new UsernameNotFoundException("Usuário não encontrado");
                    });
            
            // 3. Verificar se usuário está ativo
            if (!user.isEnabled()) {
                log.error("Usuário inativo tentou renovar token:  {}", username);
                throw new BadCredentialsException("Usuário inativo");
            }
            
            // 4. Validar refresh token
            if (!jwtService.isRefreshTokenValid(request.getRefreshToken(), user)) {
                log.error("Refresh token inválido para usuário: {}", username);
                throw new BadCredentialsException("Refresh token inválido ou expirado");
            }
            
            // 5. Gerar novos tokens
            String newAccessToken = jwtService. generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            
            log.info("Tokens renovados com sucesso para usuário: {}", username);
            
            // 6. Montar resposta
            return AuthResponse. builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getJwtExpiration() / 1000)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user. getEmail())
                    .fullName(user.getFullName())
                    .roles(user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet()))
                    .issuedAt(LocalDateTime.now())
                    .build();
                    
        } catch (ExpiredJwtException e) {
            log.error("Refresh token expirado: {}", e.getMessage());
            throw new BadCredentialsException("Refresh token expirado");
        } catch (JwtException e) {
            log.error("Erro ao processar refresh token: {}", e. getMessage());
            throw new BadCredentialsException("Refresh token inválido");
        }
    }
}