package br.com.glprevenda.security.service;

import br.com.glprevenda. security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security. core.userdetails.UserDetailsService;
import org.springframework. security.core.userdetails. UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço customizado para carregar dados do usuário do banco. 
 * 
 * Implementa UserDetailsService (interface do Spring Security).
 * 
 * Spring Security chama loadUserByUsername() durante o login para: 
 * 1. Buscar usuário no banco
 * 2. Verificar senha
 * 3. Carregar roles (permissões)
 * 
 * @author Ozeias
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Carrega usuário pelo username ou email.
     * 
     * Chamado automaticamente pelo Spring Security durante autenticação.
     * 
     * @param usernameOrEmail Username ou email do usuário
     * @return UserDetails (dados do usuário + roles)
     * @throws UsernameNotFoundException Se usuário não existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log. debug("Carregando usuário:   {}", usernameOrEmail);
        
        // Busca por username OU email
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", usernameOrEmail);
                    return new UsernameNotFoundException(
                            "Usuário não encontrado:   " + usernameOrEmail
                    );
                });
    }
}