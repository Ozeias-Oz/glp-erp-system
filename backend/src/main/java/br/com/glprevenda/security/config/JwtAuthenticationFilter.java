package br.com.glprevenda.security.config;

import br.com.glprevenda. security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org. springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org. springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que intercepta TODAS as requisições HTTP. 
 * 
 * Responsabilidades:
 * 1. Extrair token JWT do header Authorization
 * 2. Validar o token
 * 3. Carregar usuário do banco
 * 4. Autenticar o usuário no Spring Security
 * 
 * Se o token for válido, o usuário fica autenticado para aquela requisição.
 * 
 * @author Ozeias
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    /**
     * Método executado em TODA requisição HTTP.
     * 
     * Fluxo:
     * 1. Verifica se tem header Authorization
     * 2. Extrai token (remove "Bearer ")
     * 3. Valida token e extrai username
     * 4. Carrega usuário do banco
     * 5. Autentica no Spring Security
     * 6. Passa requisição adiante
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Extrair header Authorization
        final String authHeader = request.getHeader("Authorization");
        
        // Se não tem header OU não começa com "Bearer ", pula filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 2. Extrair token (remove "Bearer ")
            final String jwt = authHeader.substring(7);
            
            // 3. Extrair username do token
            final String username = jwtService.extractUsername(jwt);
            
            // 4. Se tem username E usuário não está autenticado ainda
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 5. Carregar usuário do banco
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 6. Validar token
                if (jwtService. isTokenValid(jwt, userDetails)) {
                    
                    // 7. Criar objeto de autenticação
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    
                    // 8. Adicionar detalhes da requisição
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // 9. Autenticar no Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Usuário autenticado via JWT: {}", username);
                }
            }
            
        } catch (Exception e) {
            log.error("Erro ao processar token JWT:  {}", e.getMessage());
        }
        
        // 10. Passar requisição adiante (próximo filtro ou controller)
        filterChain. doFilter(request, response);
    }
}