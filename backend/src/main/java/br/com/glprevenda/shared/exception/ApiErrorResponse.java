package br.com.glprevenda. shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Resposta padrão de erro da API
 * 
 * Formato de resposta: 
 * {
 *   "success": false,
 *   "message": "Mensagem de erro",
 *   "data": { ...  },  // Opcional (ex: erros de validação)
 *   "timestamp": "2026-01-16T12:00:00"
 * }
 * 
 * @author Ozeias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    
    /**
     * Indica se a requisição foi bem-sucedida (sempre false em erros)
     */
    private Boolean success;
    
    /**
     * Mensagem de erro legível para o usuário
     */
    private String message;
    
    /**
     * Dados adicionais (ex: erros de validação de campos)
     */
    private Object data;
    
    /**
     * Timestamp do erro
     */
    private LocalDateTime timestamp;
}