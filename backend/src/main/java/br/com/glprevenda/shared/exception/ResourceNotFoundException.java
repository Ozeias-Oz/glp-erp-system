package br.com.glprevenda.shared.exception;

/**
 * Exceção lançada quando um recurso não é encontrado.
 * 
 * Exemplos de uso:
 * - throw new ResourceNotFoundException("Usuário não encontrado")
 * - throw new ResourceNotFoundException("User", 123L)
 * - throw new ResourceNotFoundException("Role", "ROLE_ADMIN")
 * 
 * @author Ozeias
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Construtor com mensagem customizada
     * 
     * @param message Mensagem de erro
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Construtor para recursos identificados por ID numérico
     * 
     * @param resource Nome do recurso (ex: "User", "Role")
     * @param id ID do recurso
     */
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s com ID %d não encontrado", resource, id));
    }
    
    /**
     * Construtor para recursos identificados por String
     * 
     * @param resource Nome do recurso (ex: "Role", "Permission")
     * @param identifier Identificador (ex: "ROLE_ADMIN", "READ_USERS")
     */
    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s '%s' não encontrado", resource, identifier));
    }
    
    /**
     * Construtor para recursos identificados por campo + valor
     * 
     * @param resource Nome do recurso
     * @param field Nome do campo
     * @param value Valor buscado
     */
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s não encontrado com %s:  %s", resource, field, value));
    }
}