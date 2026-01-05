package br.com.glprevenda.security.repository;

import br.com.glprevenda.security.entity. Role;
import org.springframework. data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações de banco de dados com a entidade Role. 
 * 
 * JpaRepository<Role, Long> fornece automaticamente:
 * - save(role)           → Salvar/Atualizar
 * - findById(id)         → Buscar por ID
 * - findAll()            → Listar todos
 * - deleteById(id)       → Deletar por ID
 * - count()              → Contar registros
 * - existsById(id)       → Verificar se existe
 * 
 * Métodos customizados seguem convenção de nomenclatura do Spring Data:
 * - findByNome → SELECT * FROM roles WHERE name = ?
 * 
 * @author Ozeias
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Busca role pelo nome
     * Exemplo: findByName("ROLE_ADMIN")
     * 
     * SQL gerado automaticamente:
     * SELECT * FROM roles WHERE name = ?
     * 
     * @param name Nome da role (ex:  ROLE_ADMIN)
     * @return Optional<Role> (vazio se não encontrar)
     */
    Optional<Role> findByName(String name);
    
    /**
     * Verifica se existe role com determinado nome
     * 
     * SQL gerado: 
     * SELECT COUNT(*) > 0 FROM roles WHERE name = ?
     * 
     * @param name Nome da role
     * @return true se existir, false se não
     */
    boolean existsByName(String name);
}