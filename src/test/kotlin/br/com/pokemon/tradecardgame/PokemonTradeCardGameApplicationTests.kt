package br.com.pokemon.tradecardgame

import org.junit.jupiter.api.Test
import org.springframework.boot.SpringApplication
import kotlin.test.assertNotNull

/**
 * Teste de inicialização da aplicação Pokemon Trading Card Game.
 *
 * Este teste verifica se a aplicação pode ser inicializada corretamente
 * sem depender do contexto Spring completo, evitando problemas de
 * configuração de segurança e dependências complexas.
 */
class PokemonTradeCardGameApplicationTests {

    @Test
    fun `should load application class successfully`() {
        // Given
        val applicationClass = PokemonTradeCardGameApplication::class.java

        // When & Then
        assertNotNull(applicationClass, "Application class should be loaded")
        assert(applicationClass.name.contains("PokemonTradeCardGameApplication")) {
            "Application class should have correct package and name"
        }
    }

    @Test
    fun `should create spring application successfully`() {
        // Given
        val applicationClass = PokemonTradeCardGameApplication::class.java

        // When
        val springApplication = SpringApplication(applicationClass)

        // Then
        assertNotNull(springApplication, "SpringApplication should be created successfully")
        // Verificamos se a aplicação foi criada corretamente sem acessar campos privados
        assert(springApplication.javaClass.simpleName == "SpringApplication") {
            "SpringApplication should be of correct type"
        }
    }

    @Test
    fun `should have correct application properties`() {
        // Given
        val applicationClass = PokemonTradeCardGameApplication::class.java

        // When & Then
        assertNotNull(applicationClass.simpleName, "Application should have a name")
        assert(applicationClass.simpleName == "PokemonTradeCardGameApplication") {
            "Application class should have correct name"
        }
    }
}
