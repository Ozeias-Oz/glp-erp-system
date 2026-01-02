package br.com.glprevenda.shared.exception;

/**
 * Exceção lançada quando um recurso não é encontrado no sistema.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s com ID %d não encontrado", resource, id));
    }
}
