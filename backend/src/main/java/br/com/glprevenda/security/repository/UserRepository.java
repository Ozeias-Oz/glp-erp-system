package br.com.glprevenda.security.repository;

import br.com.glprevenda.security. entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework. data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype. Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de banco de dados com a entidade User. 
 * 
 * Além dos métodos herdados de JpaRepository, adiciona queries customizadas
 * para buscar usuários por diferentes critérios.
 * 
 * @author Ozeias
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ==================== MÉTODOS BÁSICOS ====================
    
    /**
     * Busca usuário pelo username
     * Usado pelo Spring Security durante o login
     * 
     * SQL gerado:  
     * SELECT * FROM users WHERE username = ?
     * 
     * @param username Nome de usuário
     * @return Optional<User> (vazio se não encontrar)
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Busca usuário pelo email
     * 
     * SQL gerado: 
     * SELECT * FROM users WHERE email = ?
     * 
     * @param email Email do usuário
     * @return Optional<User> (vazio se não encontrar)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica se existe usuário com determinado username
     * 
     * SQL gerado:
     * SELECT COUNT(*) > 0 FROM users WHERE username = ? 
     * 
     * @param username Nome de usuário
     * @return true se existir, false se não
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica se existe usuário com determinado email
     * 
     * SQL gerado:  
     * SELECT COUNT(*) > 0 FROM users WHERE email = ?
     * 
     * @param email Email
     * @return true se existir, false se não
     */
    boolean existsByEmail(String email);
    
    // ==================== MÉTODOS SEM PAGINAÇÃO ====================
    
    /**
     * Lista apenas usuários ativos
     * 
     * SQL gerado:
     * SELECT * FROM users WHERE active = true
     * 
     * @return Lista de usuários ativos
     */
    List<User> findByActiveTrue();
    
    /**
     * Busca usuários por role específica
     * 
     * @Query → JPQL (Java Persistence Query Language)
     * JOIN FETCH → Carrega roles junto (evita N+1 queries)
     * 
     * JPQL traduzido:
     * SELECT u FROM User u 
     * JOIN u.roles r 
     * WHERE r.name = : roleName
     * 
     * SQL gerado:
     * SELECT u.* FROM users u
     * INNER JOIN user_roles ur ON u.id = ur. user_id
     * INNER JOIN roles r ON ur.role_id = r.id
     * WHERE r.name = ? 
     * 
     * @param roleName Nome da role (ex:  ROLE_ADMIN)
     * @return Lista de usuários com essa role
     */
    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.roles r WHERE r.name = : roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * Busca usuários por parte do nome ou email (busca flexível)
     * 
     * JPQL com LOWER() e LIKE para busca case-insensitive
     * 
     * SQL gerado: 
     * SELECT * FROM users 
     * WHERE LOWER(full_name) LIKE LOWER(?)
     *    OR LOWER(email) LIKE LOWER(?)
     * 
     * Exemplo de uso:
     * findByFullNameOrEmailContaining("silva")
     * → Encontra:  "João Silva", "silva@email.com", "Maria SILVA"
     * 
     * @param search Termo de busca
     * @return Lista de usuários encontrados
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchByNameOrEmail(@Param("search") String search);
    
    // ==================== NOVOS MÉTODOS COM PAGINAÇÃO ====================
    
    /**
     * Lista apenas usuários ativos com paginação
     * 
     * SQL gerado:
     * SELECT * FROM users WHERE active = true
     * LIMIT ?  OFFSET ?
     * 
     * @param pageable Parâmetros de paginação (página, tamanho, ordenação)
     * @return Página de usuários ativos
     */
    Page<User> findByActiveTrue(Pageable pageable);
    
    /**
     * Busca usuários por email (case-insensitive) com paginação
     * 
     * SQL gerado: 
     * SELECT * FROM users 
     * WHERE LOWER(email) LIKE LOWER(?)
     * LIMIT ? OFFSET ?
     * 
     * @param email Parte do email a buscar
     * @param pageable Parâmetros de paginação
     * @return Página de usuários encontrados
     */
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    
    /**
     * Busca usuários por username (case-insensitive) com paginação
     * 
     * SQL gerado:
     * SELECT * FROM users 
     * WHERE LOWER(username) LIKE LOWER(?)
     * LIMIT ? OFFSET ?
     * 
     * @param username Parte do username a buscar
     * @param pageable Parâmetros de paginação
     * @return Página de usuários encontrados
     */
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    
    /**
     * Busca usuários por nome completo (case-insensitive) com paginação
     * 
     * SQL gerado: 
     * SELECT * FROM users 
     * WHERE LOWER(full_name) LIKE LOWER(?)
     * LIMIT ? OFFSET ?
     * 
     * @param fullName Parte do nome a buscar
     * @param pageable Parâmetros de paginação
     * @return Página de usuários encontrados
     */
    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);
    
    /**
     * Busca usuários por nome ou email com paginação
     * 
     * SQL gerado:
     * SELECT * FROM users 
     * WHERE LOWER(full_name) LIKE LOWER(?)
     *    OR LOWER(email) LIKE LOWER(?)
     * LIMIT ? OFFSET ?
     * 
     * @param search Termo de busca
     * @param pageable Parâmetros de paginação
     * @return Página de usuários encontrados
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchByNameOrEmail(@Param("search") String search, Pageable pageable);
}