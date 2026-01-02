-- Tabela de usuários (preparação para módulo de segurança)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);

-- Comentários na tabela
COMMENT ON TABLE users IS 'Tabela de usuários do sistema ERP GLP';
COMMENT ON COLUMN users.id IS 'Identificador único do usuário';
COMMENT ON COLUMN users.username IS 'Nome de usuário para login';
COMMENT ON COLUMN users.email IS 'Email do usuário';
COMMENT ON COLUMN users.password IS 'Senha criptografada (BCrypt)';
COMMENT ON COLUMN users.active IS 'Indica se o usuário está ativo no sistema';
