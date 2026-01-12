package br.com.glprevenda.security.controller;

import br.com.glprevenda.security. dto.LoginRequest;
import br.com. glprevenda.security.dto.RefreshTokenRequest;
import br. com.glprevenda.security.dto.RegisterRequest;
import br.com. glprevenda.security.dto.AuthResponse;
import br.com. glprevenda.security.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito. MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java. util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito. Mockito.*;
import static org. springframework.test.web.servlet. request.MockMvcRequestBuilders.*;
import static org.springframework. test.web.servlet.result. MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Desabilita filtros de segurança
@DisplayName("AuthController - Testes de Integração")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login - Deve fazer login com sucesso")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .roles(Set.of("ROLE_VENDEDOR"))
                .issuedAt(LocalDateTime.now())
                .build();
        
        when(authService. login(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.userId").value(1));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve retornar 401 para credenciais inválidas")
    void shouldReturn401ForInvalidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve retornar 400 para campos vazios")
    void shouldReturn400ForEmptyFields() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("", "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("POST /api/auth/register - Deve registrar usuário com sucesso")
    void shouldRegisterSuccessfully() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "new@example.com",
                "password123",
                "New User"
        );
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(2L)
                .username("newuser")
                .email("new@example.com")
                .fullName("New User")
                .roles(Set.of("ROLE_VENDEDOR"))
                .issuedAt(LocalDateTime.now())
                .build();
        
        when(authService. register(any(RegisterRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.userId").value(2));

        verify(authService).register(any(RegisterRequest. class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Deve retornar 400 para email inválido")
    void shouldReturn400ForInvalidEmail() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "invalid-email",
                "password123",
                "New User"
        );

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/register - Deve retornar 400 para senha curta")
    void shouldReturn400ForShortPassword() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "new@example.com",
                "123",  // Senha muito curta
                "New User"
        );

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Deve renovar tokens com sucesso")
    void shouldRefreshTokensSuccessfully() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        AuthResponse response = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .roles(Set.of("ROLE_VENDEDOR"))
                .issuedAt(LocalDateTime.now())
                .build();
        
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        // When & Then
        mockMvc. perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Deve retornar 401 para refresh token inválido")
    void shouldReturn401ForInvalidRefreshToken() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");
        
        when(authService. refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new RuntimeException("Refresh token inválido"));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Deve retornar 400 para refresh token vazio")
    void shouldReturn400ForEmptyRefreshToken() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("");

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refreshToken(any());
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve aceitar login com email")
    void shouldAcceptLoginWithEmail() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .roles(Set.of("ROLE_VENDEDOR"))
                .issuedAt(LocalDateTime.now())
                .build();
        
        when(authService.login(any(LoginRequest. class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType. APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(authService).login(any(LoginRequest.class));
    }
}