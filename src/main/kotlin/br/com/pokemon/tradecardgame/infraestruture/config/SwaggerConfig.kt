package br.com.pokemon.tradecardgame.infraestruture.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Pokemon Trade Card Game Collection API")
                    .description("")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Equipe de Desenvolvimento")
                            .email("master@pokemon.com.br")
                    )
            )
    }
}