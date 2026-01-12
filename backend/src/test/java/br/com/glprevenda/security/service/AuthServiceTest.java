package br.com.glprevenda.security.service;

import br.com.glprevenda. security.dto.LoginRequest;
import br.com. glprevenda.security.dto.RefreshTokenRequest;
import br. com.glprevenda.security.dto.RegisterRequest;
import br.com. glprevenda.security.dto.AuthResponse;
import br.com. glprevenda.security.entity.User;
import br.com.glprevenda.security. entity.Role;
import br.com.glprevenda.security. repository.UserRepository;
import br. com.glprevenda.security.repository.RoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api. DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito. ArgumentMatchers.*;
import static org.mockito. Mockito.*;

@ExtendWith(MockitoExtension. class)
@DisplayName("AuthService - Testes Unitários")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role vendedorRole;

    @BeforeEach
    void setUp() {
        vendedorRole = Role.builder()
                .id(2L)
                .name("ROLE_VENDEDOR")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .active(true)
                .roles(Set.of(vendedorRole))
                .build();
    }

    @Test
    @DisplayName("Deve fazer login com sucesso usando username")
    void shouldLoginSuccessfullyWithUsername() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        // When
        AuthResponse response = authService. login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer login com credenciais inválidas")
    void shouldThrowExceptionForInvalidCredentials() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void shouldRegisterNewUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "new@example.com",
                "password123",
                "New User"
        );
        
        User newUser = User.builder()
                .id(2L)
                .username("newuser")
                .email("new@example.com")
                .password("encodedPassword")
                .fullName("New User")
                .active(true)
                .roles(Set.of(vendedorRole))
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository. existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_VENDEDOR")).thenReturn(Optional.of(vendedorRole));
        when(userRepository.save(any(User. class))).thenReturn(newUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User. class))).thenReturn("refresh-token");

        // When
        AuthResponse response = authService. register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("ROLE_VENDEDOR");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar username já existente")
    void shouldThrowExceptionForExistingUsername() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "testuser",
                "new@example.com",
                "password123",
                "New User"
        );
        
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username já está em uso");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar email já existente")
    void shouldThrowExceptionForExistingEmail() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "test@example.com",
                "password123",
                "New User"
        );
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email já está em uso");
        
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve renovar tokens com refresh token válido")
    void shouldRefreshTokensSuccessfully() {
        // Given
        String refreshToken = "valid-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.isRefreshTokenValid(refreshToken, testUser)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");

        // When
        AuthResponse response = authService.refreshToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        
        verify(jwtService).extractUsername(refreshToken);
        verify(jwtService).isRefreshTokenValid(refreshToken, testUser);
        verify(jwtService).generateToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
    }

    @Test
    @DisplayName("Deve lançar exceção ao renovar com refresh token inválido")
    void shouldThrowExceptionForInvalidRefreshToken() {
        // Given
        String refreshToken = "invalid-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        when(jwtService. extractUsername(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.isRefreshTokenValid(refreshToken, testUser)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token inválido");
        
        verify(jwtService).isRefreshTokenValid(refreshToken, testUser);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao renovar token de usuário inativo")
    void shouldThrowExceptionForInactiveUser() {
        // Given
        String refreshToken = "valid-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        User inactiveUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .active(false)  // Usuário inativo
                .roles(Set.of(vendedorRole))
                .build();
        
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));
        
        // ✅ REMOVIDO:  Stubbing desnecessário (código lança exceção antes de validar token)

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário inativo");
        
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).isRefreshTokenValid(anyString(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao renovar token de usuário não encontrado")
    void shouldThrowExceptionForUserNotFound() {
        // Given
        String refreshToken = "valid-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        when(jwtService. extractUsername(refreshToken)).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(RuntimeException. class)
                .hasMessageContaining("Usuário não encontrado");
        
        verify(jwtService, never()).isRefreshTokenValid(anyString(), any());
    }
}