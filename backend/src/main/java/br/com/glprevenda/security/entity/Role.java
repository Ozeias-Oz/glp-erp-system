package br.com.glprevenda.security.entity;

import java.time. LocalDateTime;
import java.util.HashSet;
import java.util. Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data. jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidade que representa um perfil de acesso (Role) no sistema.
 * 
 * RBAC (Role-Based Access Control):
 * - Cada role define um conjunto de permissões
 * - Um usuário pode ter múltiplas roles
 * - Exemplo:  ROLE_ADMIN, ROLE_GERENTE, ROLE_VENDEDOR
 * 
 * @author Ozeias
 */
@Entity
@Table(name = "roles")
@EqualsAndHashCode(exclude = {"users"})  // Exclui users do hashCode/equals
@ToString(exclude = {"users"})            // Exclui users do toString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;
    
    /**
     * Nome da role (deve começar com ROLE_)
     * Exemplo: ROLE_ADMIN, ROLE_VENDEDOR
     * 
     * Spring Security usa esse padrão por convenção
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    /**
     * Descrição legível da role
     * Exemplo: "Administrador do sistema - Acesso total"
     */
    @Column(length = 255)
    private String description;
    
    /**
     * Relacionamento N:N com User
     * mappedBy = "roles" → O mapeamento está na classe User
     * 
     * ⚠️ Usar Set (não List) para evitar duplicatas
     * ⚠️ @Builder. Default para inicializar vazio
     */
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();
    
    // ════════════════════════════════════════════════════
    // AUDITORIA AUTOMÁTICA (via @EnableJpaAuditing)
    // ════════════════════════════════════════════════════
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // ════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ════════════════════════════════════════════════════
    
    /**
     * Verifica se é uma role de administrador
     */
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.name);
    }
    
    /**
     * Retorna apenas o nome sem o prefixo ROLE_
     * Exemplo:  ROLE_ADMIN → ADMIN
     */
    public String getSimpleName() {
        return this.name != null && this.name.startsWith("ROLE_") // Verifica prefixo
                ? this.name.substring(5) // Remover "ROLE_"
                : this.name; // Retorna como está
    }
}