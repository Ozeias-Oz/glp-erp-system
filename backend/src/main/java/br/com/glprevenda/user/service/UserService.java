package br.com.glprevenda.user.service;

import br.com.glprevenda.security.entity.Role;
import br.com.glprevenda.security.entity. User;
import br.com. glprevenda.security.repository.RoleRepository;
import br.com.glprevenda. security.repository.UserRepository;
import br.com.glprevenda.user.dto.*;
import br.com.glprevenda. shared.exception.DuplicateResourceException;
import br.com.glprevenda.shared.exception.DuplicateResourceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org. springframework.security.crypto.password. PasswordEncoder;
import org. springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util. Set;
import java.util. stream.Collectors;

/**
 * Service para gerenciamento de usuários
 * 
 * @author Ozeias
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lista todos os usuários com paginação
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Listando usuários - página: {}, tamanho: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        return userRepository. findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    /**
     * Busca usuário por ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Buscando usuário por ID:  {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com ID: " + id));
        
        return UserResponse.fromEntity(user);
    }

    /**
     * Busca usuário por username
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.info("Buscando usuário por username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado:  " + username));
        
        return UserResponse.fromEntity(user);
    }

    /**
     * Cria novo usuário (apenas ADMIN)
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Criando novo usuário: {}", request.getUsername());

     // Validar se username já existe
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username", request.getUsername());
        }

        // Validar se email já existe
        if (userRepository. existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email", request.getEmail());
        }

        // Buscar roles
        Set<Role> roles = getRolesFromNames(
                request.getRoles() != null && ! request.getRoles().isEmpty() 
                        ? request.getRoles() 
                        : Set.of("ROLE_VENDEDOR")
        );

        // Criar usuário
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .active(request.getActive() != null ?  request.getActive() : true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuário criado com sucesso: {}", savedUser.getUsername());

        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Atualiza dados do usuário
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Atualizando usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com ID: " + id));

        // Atualizar apenas campos enviados
        if (request.getEmail() != null) {
            // Validar se email já está em uso por outro usuário
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new RuntimeException("Email já está em uso");
                        }
                    });
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        User updatedUser = userRepository.save(user);
        log.info("Usuário atualizado:  {}", updatedUser.getUsername());

        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Altera senha do usuário
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, String currentUsername, boolean isAdmin) {
        log.info("Alterando senha do usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com ID: " + id));

        // Se não é admin, validar senha atual
        if (!isAdmin) {
            if (request.getCurrentPassword() == null) {
                throw new RuntimeException("Senha atual é obrigatória");
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Senha atual inválida");
            }

            // Validar se está alterando própria senha
            if (! user.getUsername().equals(currentUsername)) {
                throw new RuntimeException("Você não tem permissão para alterar a senha deste usuário");
            }
        }

        user.setPassword(passwordEncoder.encode(request. getNewPassword()));
        userRepository.save(user);

        log.info("Senha alterada com sucesso para usuário:  {}", user.getUsername());
    }

    /**
     * Ativa ou desativa usuário (soft delete)
     */
    @Transactional
    public UserResponse updateUserStatus(Long id, boolean active) {
        log.info("Alterando status do usuário ID: {} para active={}", id, active);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com ID: " + id));

        user.setActive(active);
        User updatedUser = userRepository.save(user);

        log.info("Status do usuário {} alterado para:  {}", 
                updatedUser.getUsername(), active ? "ATIVO" : "INATIVO");

        return UserResponse. fromEntity(updatedUser);
    }

    /**
     * Atualiza roles do usuário
     */
    @Transactional
    public UserResponse updateUserRoles(Long id, UpdateRolesRequest request) {
        log.info("Atualizando roles do usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com ID: " + id));

        Set<Role> roles = getRolesFromNames(request.getRoles());
        user.setRoles(roles);

        User updatedUser = userRepository.save(user);
        log.info("Roles atualizadas para usuário {}: {}", 
                updatedUser.getUsername(), request.getRoles());

        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Desativa usuário (soft delete)
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Desativando usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com ID: " + id));

        user.setActive(false);
        userRepository.save(user);

        log.info("Usuário {} desativado (soft delete)", user.getUsername());
    }

    /**
     * Busca apenas usuários ativos
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getActiveUsers(Pageable pageable) {
        log.info("Listando usuários ativos");
        
        return userRepository. findByActiveTrue(pageable)
                .map(UserResponse::fromEntity);
    }

    /**
     * Busca usuários por email
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsersByEmail(String email, Pageable pageable) {
        log.info("Buscando usuários por email contendo: {}", email);
        
        return userRepository.findByEmailContainingIgnoreCase(email, pageable)
                .map(UserResponse::fromEntity);
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Converte nomes de roles em entidades Role
     */
    private Set<Role> getRolesFromNames(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException(
                            "Role não encontrada: " + roleName));
            roles.add(role);
        }
        
        return roles;
    }
}