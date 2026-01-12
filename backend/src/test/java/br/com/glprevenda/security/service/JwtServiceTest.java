package br.com.glprevenda.security.service;

import br.com. glprevenda.security.entity.User;
import br.com.glprevenda.security. entity.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api. DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "security.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
    "security.jwt. expiration=86400000",
    "security.jwt.refresh-expiration=604800000"
})
@DisplayName("JwtService - Testes Unitários")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;
    
    private User testUser;

    @BeforeEach
    void setUp() {
        Role adminRole = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .fullName("Test User")
                .active(true)
                .roles(Set.of(adminRole))
                .build();
    }

    @Test
    @DisplayName("Deve gerar access token com sucesso")
    void shouldGenerateAccessToken() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token).contains(".");
        
        // JWT tem 2 pontos (3 partes:  header. payload.signature)
        long dotCount = token.chars().filter(ch -> ch == '.').count();
        assertThat(dotCount).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve gerar refresh token com sucesso")
    void shouldGenerateRefreshToken() {
        // When
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken).contains(".");
        
        // JWT tem 2 pontos (3 partes: header.payload.signature)
        long dotCount = refreshToken.chars().filter(ch -> ch == '.').count();
        assertThat(dotCount).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve extrair username do token corretamente")
    void shouldExtractUsername() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Deve validar token válido")
    void shouldValidateValidToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve invalidar token com username diferente")
    void shouldInvalidateTokenWithDifferentUsername() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        User anotherUser = User.builder()
                .username("anotheruser")
                .email("another@example.com")
                .password("password")
                .fullName("Another User")
                .active(true)
                .roles(Set. of())
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, anotherUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve identificar access token corretamente")
    void shouldIdentifyAccessToken() {
        // Given
        String accessToken = jwtService.generateToken(testUser);

        // When
        boolean isRefresh = jwtService.isRefreshToken(accessToken);

        // Then
        assertThat(isRefresh).isFalse();
    }

    @Test
    @DisplayName("Deve identificar refresh token corretamente")
    void shouldIdentifyRefreshToken() {
        // Given
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // When
        boolean isRefresh = jwtService.isRefreshToken(refreshToken);

        // Then
        assertThat(isRefresh).isTrue();
    }

    @Test
    @DisplayName("Deve validar refresh token válido")
    void shouldValidateValidRefreshToken() {
        // Given
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // When
        boolean isValid = jwtService.isRefreshTokenValid(refreshToken, testUser);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve invalidar access token usado como refresh token")
    void shouldInvalidateAccessTokenAsRefreshToken() {
        // Given
        String accessToken = jwtService.generateToken(testUser);

        // When
        boolean isValid = jwtService.isRefreshTokenValid(accessToken, testUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exceção para token inválido")
    void shouldThrowExceptionForInvalidToken() {
        // Given
        String invalidToken = "invalid. token.here";

        // When & Then
        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve retornar tempo de expiração do access token")
    void shouldReturnAccessTokenExpiration() {
        // When
        Long expiration = jwtService.getJwtExpiration();

        // Then
        assertThat(expiration).isEqualTo(86400000L);
    }

    @Test
    @DisplayName("Deve retornar tempo de expiração do refresh token")
    void shouldReturnRefreshTokenExpiration() {
        // When
        Long expiration = jwtService.getRefreshExpiration();

        // Then
        assertThat(expiration).isEqualTo(604800000L);
    }

    @Test
    @DisplayName("Access token e refresh token devem ser diferentes")
    void accessTokenAndRefreshTokenShouldBeDifferent() {
        // When
        String accessToken = jwtService.generateToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertThat(accessToken).isNotEqualTo(refreshToken);
    }

    @Test
    @DisplayName("Deve extrair claims do token")
    void shouldExtractClaimsFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }
}