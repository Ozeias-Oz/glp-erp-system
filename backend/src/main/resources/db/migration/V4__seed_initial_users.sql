-- ========================================
-- MIGRATION V4: Seed - Usuários Iniciais
-- ========================================

DELETE FROM user_roles;
DELETE FROM users;

INSERT INTO users (username, email, password, full_name, active, created_at, updated_at) VALUES
('admin', 'admin@glprevenda.com.br', '$2a$10$etXcPRnKn4YmXsvVfM1R2eXNChEniALRgjAh2lCd3DLuRnepp2CVi', 'Administrador do Sistema', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ozeias', 'ozeias@glprevenda.com.br', '$2a$10$etXcPRnKn4YmXsvVfM1R2eXNChEniALRgjAh2lCd3DLuRnepp2CVi', 'Ozeias Silva', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('gerente', 'gerente@glprevenda.com. br', '$2a$10$etXcPRnKn4YmXsvVfM1R2eXNChEniALRgjAh2lCd3DLuRnepp2CVi', 'Gerente de Vendas', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('vendedor', 'vendedor@glprevenda.com.br', '$2a$10$etXcPRnKn4YmXsvVfM1R2eXNChEniALRgjAh2lCd3DLuRnepp2CVi', 'Vendedor Teste', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('financeiro', 'financeiro@glprevenda.com. br', '$2a$10$etXcPRnKn4YmXsvVfM1R2eXNChEniALRgjAh2lCd3DLuRnepp2CVi', 'Analista Financeiro', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('estoque', 'estoque@glprevenda. com.br', '$2a$10$etXcPRnKn4YmXsvVfM1R2eXNChEniALRgjAh2lCd3DLuRnepp2CVi', 'Controle de Estoque', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'ozeias' AND r.name = 'ROLE_ADMIN';
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'ozeias' AND r.name = 'ROLE_GERENTE';
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'gerente' AND r.name = 'ROLE_GERENTE';
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'vendedor' AND r.name = 'ROLE_VENDEDOR';
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'financeiro' AND r.name = 'ROLE_FINANCEIRO';
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'estoque' AND r.name = 'ROLE_ESTOQUE';