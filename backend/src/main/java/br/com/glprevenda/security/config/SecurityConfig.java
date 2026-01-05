package br.com.glprevenda.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org. springframework.security.config.annotation. web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password. PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework. security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors. UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de Segurança do Spring Security. 
 * 
 * Responsável por:
 * - Definir endpoints públicos/privados
 * - Configurar autenticação JWT
 * - Habilitar CORS
 * - Desabilitar CSRF (não precisa com JWT)
 * - Configurar BCrypt para senhas
 * 
 * @author Ozeias
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize, @Secured, etc.
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    
    /**
     * Configura a cadeia de filtros de segurança.
     * 
     * Define:
     * - Endpoints públicos (login, register)
     * - Endpoints privados (resto)
     * - Desabilita CSRF (não precisa com JWT)
     * - Sessão STATELESS (sem cookies, só JWT)
     * - Adiciona filtro JWT
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF (Cross-Site Request Forgery)
                // Não precisa com JWT pois não usa cookies
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configurar CORS
                .cors(cors -> cors. configurationSource(corsConfigurationSource()))
                
                // Configurar autorização de requisições
                .authorizeHttpRequests(auth -> auth
                        // Endpoints PÚBLICOS (não precisa autenticação)
                        .requestMatchers(
                                "/api/auth/**",              // Login, Register, Refresh
                                "/actuator/health",          // Health check
                                "/actuator/info",            // Info
                                "/error",                    // Página de erro
                                "/swagger-ui/**",            // Swagger UI
                                "/swagger-ui.html",          // Swagger HTML
                                "/v3/api-docs/**",           // OpenAPI JSON
                                "/swagger-resources/**",     // Swagger resources
                                "/webjars/**"                // Swagger webjars
                        ).permitAll()
                        
                        // Endpoints ADMIN (só ROLE_ADMIN)
                        . requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Resto precisa autenticação
                        .anyRequest().authenticated()
                )
                
                // Sessão STATELESS (sem cookies, só JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy. STATELESS)
                )
                
                // Configurar provider de autenticação
                .authenticationProvider(authenticationProvider())
                
                // Adicionar filtro JWT ANTES do filtro de autenticação padrão
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Configura CORS (Cross-Origin Resource Sharing).
     * 
     * Permite que o frontend (React/Angular) faça requisições para o backend
     * mesmo estando em origem diferente (ex: localhost:3000 → localhost:8080)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origens permitidas (frontend)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",      // React dev
                "http://localhost:4200",      // Angular dev
                "http://localhost:5173",      // Vite dev
                "http://localhost:8081"       // Homologação
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));
        
        // Permite enviar cookies/credenciais
        configuration.setAllowCredentials(true);
        
        // Aplicar para TODAS as rotas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Configura provider de autenticação.
     * 
     * Responsável por: 
     * - Carregar usuário do banco (UserDetailsService)
     * - Validar senha (PasswordEncoder)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * Bean do AuthenticationManager.
     * 
     * Usado em AuthService. login() para autenticar usuário. 
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Bean do PasswordEncoder (BCrypt).
     * 
     * BCrypt é um algoritmo de hash de senha com "salt" automático.
     * - Cada hash é único (mesmo senha igual gera hashes diferentes)
     * - Extremamente difícil de reverter
     * - Resistente a rainbow tables
     * 
     * Strength 12 = bom equilíbrio segurança/performance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}