package br.com.pokemon.tradecardgame.infraestruture.config.security

import br.com.pokemon.tradecardgame.infraestruture.dto.ErrorDetails
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

/**
 * Handles security-related exceptions for authentication and access control in the application.
 *
 * This class implements both `AuthenticationEntryPoint` and `AccessDeniedHandler` interfaces
 * to provide customized responses for authentication failures and access denial scenarios.
 *
 * - If an unauthenticated request is made, the `commence` method generates a response with
 *   details regarding the missing, invalid, or expired JWT token.
 * - If an authenticated user attempts to access forbidden resources, the `handle` method
 *   provides a customized response indicating the lack of necessary permissions.
 *
 * This handler is commonly used in a security configuration to manage exception responses
 * for applications using JWT authentication with OAuth2.
 */
@Component
class SecurityExceptionHandler(
) : AuthenticationEntryPoint, AccessDeniedHandler {
    /**
     * Companion object containing constant values for handling security-related exceptions.
     *
     * These constants are utilized across the application to provide standardized error messages
     * and HTTP status codes for authentication and authorization failures.
     *
     * Properties:
     * - AUTHORIZATION_REQUIRED: Error message indicating that authentication is required.
     * - AUTORIZATION_CODE: HTTP status code representing unauthorized access (401).
     * - UNAUTHORIZED_INVALID_JWT_MESSAGE: Error message explaining that the JWT token is missing, invalid, or expired.
     * - ACCESS_DENIED_CODE: HTTP status code representing forbidden access (403).
     * - ACCESS_DENIED_TITLE: Error message indicating that access is denied.
     * - ACCESS_DENIED_MESSAGE: Detailed error message for permission-related access denial.
     */
    companion object {
        val AUTHORIZATION_REQUIRED = "Autenticação Requerida"
        val AUTORIZATION_CODE = HttpStatus.UNAUTHORIZED.value()
        val UNAUTHORIZED_INVALID_JWT_MESSAGE =
            "O Token JWT está ausente, inválido ou expirou. Por favor, faça login novamente."

        val ACCESS_DENIED_CODE = HttpStatus.FORBIDDEN.value()
        val ACCESS_DENIED_TITLE = "Acesso não permitido"
        val ACCESS_DENIED_MESSAGE = "Você não tem permissão para acessar este recurso"
    }

    /**
     * Handles unauthorized access by generating a custom response with details about the authentication failure.
     *
     * This method is invoked when a user attempts to access a resource without valid authentication credentials
     * or when the provided JWT token is missing, invalid, or expired.
     *
     * @param request The `HttpServletRequest` that triggered the authentication failure.
     * @param response The `HttpServletResponse` used to send the error details back to the client.
     * @param authException The `AuthenticationException` containing details about the authentication error.
     */
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

    /**
     * Handles forbidden access attempts by responding with a structured error message.
     *
     * This method is invoked when a request is denied due to insufficient permissions.
     * It generates a response with HTTP status 403 (Forbidden) and includes
     * a detailed error message in JSON format.
     *
     * @param request The HttpServletRequest that triggered the access denied event.
     * @param response The HttpServletResponse used to send the error details back to the client.
     * @param accessDeniedException The AccessDeniedException containing details about the access denial.
     */
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