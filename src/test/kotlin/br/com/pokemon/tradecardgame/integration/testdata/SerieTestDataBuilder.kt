package br.com.pokemon.tradecardgame.integration.testdata

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.CreateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.UpdateSerieRequest
import br.com.pokemon.tradecardgame.domain.model.Expansion
import br.com.pokemon.tradecardgame.domain.model.Serie
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import java.time.LocalDateTime
import java.util.*

/**
 * Test data builder for creating Serie-related objects with a fluent API.
 *
 * This builder provides flexible test data creation for Serie domain objects,
 * entities, and DTOs with sensible defaults and the ability to customize
 * any field as needed for specific test scenarios.
 *
 * Usage examples:
 * ```
 * // Create with defaults
 * val serie = SerieTestDataBuilder().buildDomain()
 *
 * // Create with custom values
 * val customSerie = SerieTestDataBuilder()
 *     .withCode("CUSTOM01")
 *     .withName("Custom Serie")
 *     .withReleaseYear(2023)
 *     .buildEntity()
 *
 * // Create request DTO
 * val request = SerieTestDataBuilder()
 *     .withCode("REQ01")
 *     .buildCreateRequest()
 * ```
 */
class SerieTestDataBuilder {
    private var id: UUID? = UUID.randomUUID()
    private var code: String = "TEST${Random().nextInt(1000).toString().padStart(3, '0')}"
    private var name: String = "Test Serie ${Random().nextInt(100)}"
    private var releaseYear: Int = 2024
    private var imageUrl: String? = "https://example.com/test-serie-image.jpg"
    private var expansions: List<Expansion> = emptyList()
    private var createdAt: LocalDateTime? = LocalDateTime.now()
    private var updatedAt: LocalDateTime? = null

    /**
     * Sets the ID for the Serie.
     * @param id The UUID identifier
     * @return This builder instance for method chaining
     */
    fun withId(id: UUID?) = apply { this.id = id }

    /**
     * Sets the code for the Serie.
     * @param code The unique serie code
     * @return This builder instance for method chaining
     */
    fun withCode(code: String) = apply { this.code = code }

    /**
     * Sets the name for the Serie.
     * @param name The serie name
     * @return This builder instance for method chaining
     */
    fun withName(name: String) = apply { this.name = name }

    /**
     * Sets the release year for the Serie.
     * @param year The release year
     * @return This builder instance for method chaining
     */
    fun withReleaseYear(year: Int) = apply { this.releaseYear = year }

    /**
     * Sets the image URL for the Serie.
     * @param url The image URL (can be null)
     * @return This builder instance for method chaining
     */
    fun withImageUrl(url: String?) = apply { this.imageUrl = url }

    /**
     * Sets the expansions for the Serie.
     * @param expansions List of expansions
     * @return This builder instance for method chaining
     */
    fun withExpansions(expansions: List<Expansion>) = apply { this.expansions = expansions }

    /**
     * Sets the created timestamp for the Serie.
     * @param createdAt The creation timestamp
     * @return This builder instance for method chaining
     */
    fun withCreatedAt(createdAt: LocalDateTime?) = apply { this.createdAt = createdAt }

    /**
     * Sets the updated timestamp for the Serie.
     * @param updatedAt The update timestamp
     * @return This builder instance for method chaining
     */
    fun withUpdatedAt(updatedAt: LocalDateTime?) = apply { this.updatedAt = updatedAt }

    /**
     * Builds a Serie domain object with the configured properties.
     * @return A Serie domain object
     */
    fun buildDomain(): Serie = Serie(
        id = id,
        code = code,
        name = name,
        releaseYear = releaseYear,
        imageUrl = imageUrl,
        expansions = expansions,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    /**
     * Builds a SerieEntity with the configured properties.
     * @return A SerieEntity for persistence operations
     */
    fun buildEntity(): SerieEntity = SerieEntity(
        id = id,
        code = code,
        name = name,
        releaseYear = releaseYear,
        imageUrl = imageUrl,
        expansions = null, // SerieEntity uses String for expansions
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    /**
     * Builds a CreateSerieRequest DTO with the configured properties.
     * @return A CreateSerieRequest for API testing
     */
    fun buildCreateRequest(): CreateSerieRequest = CreateSerieRequest(
        code = code,
        name = name,
        releaseYear = releaseYear,
        imageUrl = imageUrl,
        expansions = expansions
    )

    /**
     * Builds an UpdateSerieRequest DTO with the configured properties.
     * @return An UpdateSerieRequest for API testing
     */
    fun buildUpdateRequest(): UpdateSerieRequest = UpdateSerieRequest(
        id = id,
        code = code,
        name = name,
        releaseYear = releaseYear,
        imageUrl = imageUrl,
        expansions = expansions
    )

    companion object {
        /**
         * Creates a builder with minimal valid data for testing edge cases.
         * @return A SerieTestDataBuilder with minimal configuration
         */
        fun minimal(): SerieTestDataBuilder = SerieTestDataBuilder()
            .withCode("MIN01")
            .withName("Minimal Serie")
            .withReleaseYear(1999)
            .withImageUrl(null)
            .withExpansions(emptyList())

        /**
         * Creates a builder with complete data for comprehensive testing.
         * @return A SerieTestDataBuilder with full configuration
         */
        fun complete(): SerieTestDataBuilder = SerieTestDataBuilder()
            .withCode("COMP01")
            .withName("Complete Test Serie")
            .withReleaseYear(2024)
            .withImageUrl("https://example.com/complete-serie.jpg")
            .withExpansions(emptyList()) // Would include expansions in real scenario

        /**
         * Creates a builder for testing invalid data scenarios.
         * Note: This creates data that should fail validation.
         * @return A SerieTestDataBuilder with invalid configuration
         */
        fun invalid(): SerieTestDataBuilder = SerieTestDataBuilder()
            .withCode("") // Invalid: empty code
            .withName("") // Invalid: empty name
            .withReleaseYear(1990) // Invalid: year too early

        /**
         * Creates multiple unique Serie builders for batch testing.
         * @param count The number of builders to create
         * @return A list of SerieTestDataBuilder instances with unique data
         */
        fun createMultiple(count: Int): List<SerieTestDataBuilder> {
            return (1..count).map { index ->
                SerieTestDataBuilder()
                    .withCode("BATCH${index.toString().padStart(3, '0')}")
                    .withName("Batch Serie $index")
                    .withReleaseYear(2020 + (index % 5))
            }
        }
    }
}