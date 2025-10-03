package br.com.pokemon.tradecardgame.integration.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import java.time.Instant
import java.util.*

/**
 * Test security configuration that provides mock JWT authentication for integration tests.
 *
 * This configuration:
 * - Disables OAuth2 for internal tests by providing a mock JWT decoder
 * - Configures test-specific security filters and authentication
 * - Allows all requests for simplified testing while maintaining security structure
 * - Provides utilities for generating test JWT tokens
 */
@TestConfiguration
@EnableWebSecurity
class TestSecurityConfig {

    /**
     * Mock JWT decoder that creates valid JWT tokens for testing purposes.
     * This decoder bypasses external OAuth2 server validation and creates
     * test tokens with predefined claims and authorities.
     *
     * @return A JwtDecoder that creates mock JWT tokens for testing
     */
    @Bean
    @Primary
    fun testJwtDecoder(): JwtDecoder {
        return JwtDecoder { token ->
            createMockJwt(token)
        }
    }

    /**
     * JWT authentication converter configured for test environment.
     * Converts JWT tokens to authentication objects with test authorities.
     *
     * @return A JwtAuthenticationConverter for test authentication
     */
    @Bean
    fun testJwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        }
        return converter
    }

    /**
     * Security filter chain configuration for integration tests.
     * Permits all requests to simplify testing while maintaining the security structure.
     *
     * @param http The HttpSecurity configuration
     * @return The configured SecurityFilterChain for tests
     */
    @Bean
    @Primary
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }

            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }

            oauth2ResourceServer {
                jwt {
                    jwtDecoder = testJwtDecoder()
                    jwtAuthenticationConverter = testJwtAuthenticationConverter()
                }
            }
        }
        return http.build()
    }

    /**
     * Creates a mock JWT token with predefined claims for testing.
     *
     * @param token The token string (can be any value for testing)
     * @return A mock Jwt object with test claims
     */
    private fun createMockJwt(token: String): Jwt {
        val headers = mapOf("alg" to "RS256", "typ" to "JWT")
        val claims = mapOf(
            "sub" to "test-user-id",
            "iss" to "http://localhost:8080",
            "aud" to listOf("pokemon-tcg-api"),
            "exp" to Instant.now().plusSeconds(3600).epochSecond,
            "iat" to Instant.now().epochSecond,
            "scope" to "read write",
            "authorities" to listOf("ROLE_USER")
        )

        return Jwt.withTokenValue(token)
            .headers { it.putAll(headers) }
            .claims { it.putAll(claims) }
            .build()
    }

    companion object {
        /**
         * Generates a test JWT token string for use in integration tests.
         * This token will be processed by the mock JWT decoder.
         *
         * @param subject The subject claim for the token (defaults to "test-user")
         * @return A test JWT token string
         */
        fun generateTestJwtToken(subject: String = "test-user"): String {
            return "test.jwt.token-$subject-${UUID.randomUUID()}"
        }

        /**
         * Creates a JwtAuthenticationToken for testing purposes.
         *
         * @param subject The subject for the token
         * @return A JwtAuthenticationToken for testing
         */
        fun createTestAuthenticationToken(subject: String = "test-user"): JwtAuthenticationToken {
            val jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("sub", subject)
                .claim("scope", "read write")
                .build()

            return JwtAuthenticationToken(jwt, listOf(SimpleGrantedAuthority("ROLE_USER")))
        }
    }
}