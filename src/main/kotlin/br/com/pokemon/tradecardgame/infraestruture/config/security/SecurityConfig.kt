package br.com.pokemon.tradecardgame.infraestruture.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

/**
 * Configuration class for setting up security in the application.
 *
 * This class defines the security settings, including request authorization rules,
 * CSRF protection, and OAuth2 Resource Server configuration for JWT-based authentication.
 *
 * @constructor Creates an instance of SecurityConfig.
 * @param securityExceptionHandler Handler for customizing authentication and access denied responses.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val securityExceptionHandler: SecurityExceptionHandler
) {
    /**
     * Configures the security filter chain for the application.
     * This includes disabling CSRF protection, configuring request authorization rules,
     * and setting up OAuth2 Resource Server with JWT-based authentication handlers.
     *
     * @param http The HttpSecurity object used to configure security for HTTP requests.
     * @return The configured SecurityFilterChain instance.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }

            authorizeHttpRequests {
                authorize("/actuator/health", permitAll)
                authorize("/actuator/info", permitAll)

                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/swagger-resources/**", permitAll)
                authorize("/webjars/**", permitAll)
                authorize(anyRequest, authenticated)
            }

            oauth2ResourceServer {
                jwt {
                    authenticationEntryPoint = securityExceptionHandler
                    accessDeniedHandler = securityExceptionHandler
                }
            }
        }
        return http.build()
    }
}