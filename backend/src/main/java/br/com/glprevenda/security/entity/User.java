package br.com.glprevenda.security.entity;

import java.time.LocalDateTime;
import java. util.Collection;
import java.util.HashSet;
import java.util. Set;
import java.util. stream.Collectors;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
 * Entidade que representa um usuário do sistema.
 * 
 * Implementa UserDetails (Spring Security) para integração com autenticação. 
 * 
 * @author Ozeias
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"roles"})  // Exclui roles do hashCode/equals
@ToString(exclude = {"roles"}) 
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * Senha criptografada com BCrypt
     * Nunca armazene senhas em texto plano!
     */
    @Column(nullable = false)
    private String password;
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    // ════════════════════════════════════════════════════
    // RELACIONAMENTO COM ROLES (N:N)
    // ════════════════════════════════════════════════════
    
    /**
     * Relacionamento N:N com Role
     * 
     * @JoinTable → Define a tabela intermediária user_roles
     * joinColumns → Coluna que referencia User (user_id)
     * inverseJoinColumns → Coluna que referencia Role (role_id)
     * 
     * FetchType.EAGER → Carrega roles junto com o usuário
     * (importante para Spring Security verificar permissões)
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    // ════════════════════════════════════════════════════
    // AUDITORIA AUTOMÁTICA
    // ════════════════════════════════════════════════════
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // ════════════════════════════════════════════════════
    // IMPLEMENTAÇÃO DO UserDetails (Spring Security)
    // ════════════════════════════════════════════════════
    
    /**
     * Retorna as permissões (authorities) do usuário
     * Converte Set<Role> em Collection<GrantedAuthority>
     * 
     * Spring Security usa isso para verificar @PreAuthorize, hasRole(), etc.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // Converte roles em GrantedAuthority
        return roles.stream() // Stream de roles
                .map(role -> new SimpleGrantedAuthority(role.getName())) // Converte cada Role em SimpleGrantedAuthority
                .collect(Collectors.toSet()); // Coleta em um Set
    }
    
    /**
     * A conta está expirada?
     * Retornamos true = nunca expira
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    /**
     * A conta está bloqueada?
     * Retornamos true = nunca bloqueia
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    /**
     * As credenciais estão expiradas?
     * Retornamos true = senha nunca expira
     * 
     * (Em sistema real, pode implementar expiração de senha)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    /**
     * O usuário está ativo?
     * Retorna o campo "active" do banco
     */
    @Override
    public boolean isEnabled() {
        return active;
    }
    
    // ════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ════════════════════════════════════════════════════
    
    /**
     * Adiciona uma role ao usuário
     */
    public void addRole(Role role) {
        this.roles.add(role); // Adiciona a role ao conjunto de roles do usuário
        role.getUsers().add(this); // Adiciona o usuário ao conjunto de usuários da role
    }
    
    /**
     * Remove uma role do usuário
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }
    
    /**
     * Verifica se o usuário tem uma role específica
     */
    public boolean hasRole(String roleName) {
        return roles. stream()
                .anyMatch(role -> role.getName().equals(roleName)); // Verifica se alguma role tem o nome especificado
    }
    
    /**
     * Verifica se é administrador
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
