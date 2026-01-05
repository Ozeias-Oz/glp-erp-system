package br.com.glprevenda. security.repository;

import br. com.glprevenda.security.entity.User;
import org.springframework.data.jpa. repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype. Repository;

import java.util. List;
import java.util. Optional;

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
     * WHERE r.name = :roleName
     * 
     * SQL gerado:
     * SELECT u.* FROM users u
     * INNER JOIN user_roles ur ON u.id = ur.user_id
     * INNER JOIN roles r ON ur.role_id = r.id
     * WHERE r.name = ? 
     * 
     * @param roleName Nome da role (ex: ROLE_ADMIN)
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
     * @param fullName Parte do nome
     * @param email Parte do email
     * @return Lista de usuários encontrados
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchByNameOrEmail(@Param("search") String search);
}