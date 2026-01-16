package br.com.glprevenda.shared.exception;

import lombok.extern.slf4j. Slf4j;
import org. springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework. security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind. MethodArgumentNotValidException;
import org.springframework.web.bind. annotation.ExceptionHandler;
import org.springframework.web.bind. annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Tratamento global de exceções da API
 * 
 * Captura exceções lançadas pelos controllers e retorna
 * respostas padronizadas em JSON
 * 
 * @author Ozeias
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções de recurso não encontrado (404 Not Found)
     * 
     * Exemplo:  Usuário com ID 999 não existe
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {
        
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        
        ApiErrorResponse error = ApiErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Trata exceções de recurso duplicado (409 Conflict)
     * 
     * Exemplo: Username "ozeias" já está em uso
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex) {
        
        log.warn("Recurso duplicado:  {}", ex.getMessage());
        
        ApiErrorResponse error = ApiErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Trata erros de validação (400 Bad Request)
     * 
     * Exemplo: Campo "email" inválido, campo "password" obrigatório
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Erro de validação: {}", errors);
        
        ApiErrorResponse error = ApiErrorResponse.builder()
                .success(false)
                .message("Erro de validação nos campos")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Trata exceções de autenticação (401 Unauthorized)
     * 
     * Exemplo:  Senha incorreta, usuário não existe
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex) {
        
        log.warn("Erro de autenticação: {}", ex.getMessage());
        
        ApiErrorResponse error = ApiErrorResponse. builder()
                .success(false)
                .message("Credenciais inválidas")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Trata exceções de acesso negado (403 Forbidden)
     * 
     * Exemplo: Vendedor tentando acessar endpoint de ADMIN
     */
    @ExceptionHandler(AccessDeniedException. class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex) {
        
        log.warn("Acesso negado: {}", ex.getMessage());
        
        ApiErrorResponse error = ApiErrorResponse.builder()
                .success(false)
                .message("Acesso negado.  Você não tem permissão para acessar este recurso.")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Trata RuntimeException genérica (500 Internal Server Error)
     * 
     * Exemplo:  Erro ao salvar no banco, erro de lógica
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex) {
        
        log.error("Erro interno: {}", ex.getMessage(), ex);
        
        ApiErrorResponse error = ApiErrorResponse.builder()
                .success(false)
                .message("Erro interno do servidor.  Contate o administrador.")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Trata todas as outras exceções (500 Internal Server Error)
     * 
     * Fallback para erros não mapeados
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex) {
        
        log.error("Erro inesperado: {}", ex. getMessage(), ex);
        
        ApiErrorResponse error = ApiErrorResponse.builder()
                .success(false)
                .message("Erro inesperado. Contate o administrador.")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
