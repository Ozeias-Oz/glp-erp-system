package br.com.glprevenda.shared.exception;

/**
 * Exceção lançada quando tenta criar recurso duplicado
 * (ex: username ou email já existe)
 * 
 * @author Ozeias
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String field, String value) {
        super(String.format("%s já está em uso:   %s", field, value));
    }
}