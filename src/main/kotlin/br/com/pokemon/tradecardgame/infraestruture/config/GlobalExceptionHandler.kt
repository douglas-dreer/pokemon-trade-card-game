package br.com.pokemon.tradecardgame.infraestruture.config

import br.com.pokemon.tradecardgame.domain.exception.DomainException
import br.com.pokemon.tradecardgame.domain.exception.InvalidDataException
import br.com.pokemon.tradecardgame.domain.exception.SeriesAlreadyExistsException
import br.com.pokemon.tradecardgame.domain.exception.SeriesNotFoundException
import br.com.pokemon.tradecardgame.infraestruture.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    // Função utilitária para montar a resposta de erro padronizada
    private fun createErrorResponse(status: HttpStatus, ex: Exception, request: WebRequest): ErrorResponse {
        return ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            message = ex.message ?: "Erro interno desconhecido."
        )
    }

    // 404 NOT FOUND: Tratamento para recursos não encontrados
    @ExceptionHandler(SeriesNotFoundException::class)
    fun handleNotFoundException(ex: SeriesNotFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val response = createErrorResponse(HttpStatus.NOT_FOUND, ex, request)
        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    // 409 CONFLICT: Tratamento para regras de unicidade e duplicação
    @ExceptionHandler(SeriesAlreadyExistsException::class)
    fun handleConflictException(ex: SeriesAlreadyExistsException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val response = createErrorResponse(HttpStatus.CONFLICT, ex, request)
        return ResponseEntity(response, HttpStatus.CONFLICT)
    }

    // 400 BAD REQUEST: Tratamento para dados inválidos ou erros gerais do Domain
    @ExceptionHandler(InvalidDataException::class, DomainException::class)
    fun handleBadRequestDomainException(ex: DomainException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val response = createErrorResponse(HttpStatus.BAD_REQUEST, ex, request)
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
}