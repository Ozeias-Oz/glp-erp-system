-- ════════════════════════════════════════════════════════
-- Migration V2: Sistema de permissões (RBAC - Role-Based Access Control)
-- Autor:  Ozeias
-- Data: 2024-01-02
-- Descrição: Tabelas para controle de perfis e permissões
-- ════════════════════════════════════════════════════════

-- Tabela de perfis (roles)
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de relacionamento N:N (um usuário pode ter várias roles)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    
    PRIMARY KEY (user_id, role_id),
    -- Chave composta impede duplicação (mesmo usuário + mesma role)
    
    CONSTRAINT fk_user_roles_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    -- Se deletar usuário, deleta suas roles também
    
    CONSTRAINT fk_user_roles_role 
        FOREIGN KEY (role_id) 
        REFERENCES roles(id) 
        ON DELETE CASCADE
    -- Se deletar role, remove de todos os usuários
);

-- ════════════════════════════════════════════════════════
-- ÍNDICES (melhoram performance)
-- ════════════════════════════════════════════════════════

CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- ════════════════════════════════════════════════════════
-- DADOS INICIAIS (roles padrão do sistema)
-- ════════════════════════════════════════════════════════

INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN', 'Administrador do sistema - Acesso total'),
    ('ROLE_GERENTE', 'Gerente da revendedora - Relatórios e configurações'),
    ('ROLE_VENDEDOR', 'Vendedor/Motorista - Vendas e entregas'),
    ('ROLE_FINANCEIRO', 'Acesso ao módulo financeiro'),
    ('ROLE_ESTOQUE', 'Controle de estoque de botijões');

-- ════════════════════════════════════════════════════════
-- COMENTÁRIOS (documentação no banco)
-- ════════════════════════════════════════════════════════

COMMENT ON TABLE roles IS 'Perfis de acesso do sistema (RBAC)';
COMMENT ON COLUMN roles.name IS 'Nome da role (deve começar com ROLE_)';
COMMENT ON COLUMN roles.description IS 'Descrição do que a role permite fazer';

COMMENT ON TABLE user_roles IS 'Relacionamento N:N entre usuários e perfis';
COMMENT ON COLUMN user_roles.user_id IS 'Referência ao usuário';
COMMENT ON COLUMN user_roles.role_id IS 'Referência ao perfil';