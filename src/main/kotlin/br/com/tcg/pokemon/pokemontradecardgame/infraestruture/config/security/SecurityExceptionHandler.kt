package br.com.tcg.pokemon.pokemontradecardgame.infraestruture.config.security

import br.com.tcg.pokemon.pokemontradecardgame.infraestruture.dto.ErrorDetails
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

import org.springframework.http.MediaType
import org.springframework.http.HttpStatus

@Component
class SecurityExceptionHandler(
) : AuthenticationEntryPoint, AccessDeniedHandler {

    companion object {
        val AUTHORIZATION_REQUIRED = "Autenticação Requerida"
        val AUTORIZATION_CODE = HttpStatus.UNAUTHORIZED.value()
        val UNAUTHORIZED_INVALID_JWT_MESSAGE = "O Token JWT está ausente, inválido ou expirou. Por favor, faça login novamente."

        val ACCESS_DENIED_CODE = HttpStatus.FORBIDDEN.value()
        val ACCESS_DENIED_TITLE = "Acesso não permitido"
        val ACCESS_DENIED_MESSAGE = "Você não tem permissão para acessar este recurso"
    }


    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {

        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val errorDetails = ErrorDetails(
            status = AUTORIZATION_CODE,
            error = AUTHORIZATION_REQUIRED,
            message = UNAUTHORIZED_INVALID_JWT_MESSAGE
        )

        response.writer.write(errorDetails.toJSON())
    }

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val errorDetails = ErrorDetails(
            status = ACCESS_DENIED_CODE,
            error = ACCESS_DENIED_TITLE,
            message = ACCESS_DENIED_MESSAGE
        )

        response.writer.write(errorDetails.toJSON())
    }
}