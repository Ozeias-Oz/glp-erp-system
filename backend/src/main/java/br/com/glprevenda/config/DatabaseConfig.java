package br.com.glprevenda.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuração do banco de dados e JPA.
 * 
 * @EnableJpaAuditing - Habilita auditoria automática (@CreatedDate, @LastModifiedDate)
 * @EnableTransactionManagement - Gerenciamento transacional do Spring
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    
    // Configurações adicionais podem ser adicionadas aqui
    // Por exemplo: DataSource customizado, connection pool, etc.
}
