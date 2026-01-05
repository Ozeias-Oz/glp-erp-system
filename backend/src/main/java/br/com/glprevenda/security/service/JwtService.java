package br.com.glprevenda.security. service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken. Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j. Slf4j;
import org. springframework.beans.factory.annotation. Value;
import org.springframework. security.core. GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework. stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream. Collectors;

/**
 * Serviço para geração e validação de tokens JWT. 
 * 
 * JWT (JSON Web Token) = Autenticação stateless
 * - Token contém todas as informações necessárias
 * - Não precisa consultar banco a cada requisição
 * - Assinado digitalmente (não pode ser alterado)
 * 
 * Estrutura JWT:  HEADER.PAYLOAD.SIGNATURE
 * 
 * @author Ozeias
 */
@Slf4j
@Service
public class JwtService {
    
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    
    @Value("${security.jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${security.jwt.refresh-expiration}")
    private long refreshExpiration;
    
    /**
     * Extrai o username do token JWT
     * 
     * @param token Token JWT
     * @return Username contido no token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrai uma claim específica do token
     * 
     * @param token Token JWT
     * @param claimsResolver Função para extrair a claim desejada
     * @return Valor da claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Gera token JWT para o usuário
     * 
     * @param userDetails Dados do usuário (Spring Security)
     * @return Token JWT gerado
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        
        // Adicionar roles ao token
        extraClaims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority:: getAuthority)
                .collect(Collectors.toList()));
        
        return generateToken(extraClaims, userDetails);
    }
    
    /**
     * Gera token JWT com claims customizadas
     * 
     * @param extraClaims Claims adicionais (roles, email, etc.)
     * @param userDetails Dados do usuário
     * @return Token JWT gerado
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }
    
    /**
     * Gera refresh token (validade maior)
     * 
     * @param userDetails Dados do usuário
     * @return Refresh token gerado
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }
    
    /**
     * Constrói o token JWT
     * 
     * @param extraClaims Claims adicionais
     * @param userDetails Dados do usuário
     * @param expiration Tempo de expiração
     * @return Token JWT
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        long currentTimeMillis = System.currentTimeMillis();
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(currentTimeMillis))
                .expiration(new Date(currentTimeMillis + expiration))
                .signWith(getSignInKey())
                .compact();
    }
    
    /**
     * Valida se o token é válido para o usuário
     * 
     * @param token Token JWT
     * @param userDetails Dados do usuário
     * @return true se válido, false caso contrário
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && ! isTokenExpired(token);
    }
    
    /**
     * Verifica se o token está expirado
     * 
     * @param token Token JWT
     * @return true se expirado, false caso contrário
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Extrai a data de expiração do token
     * 
     * @param token Token JWT
     * @return Data de expiração
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrai todas as claims do token
     * 
     * @param token Token JWT
     * @return Claims do token
     */
    private Claims extractAllClaims(String token) {
        return Jwts. parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Obtém a chave de assinatura do token
     * 
     * @return Chave de assinatura
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Retorna tempo de expiração do access token em segundos
     * 
     * @return Tempo de expiração em segundos
     */
    public long getExpirationInSeconds() {
        return jwtExpiration / 1000;
    }
}