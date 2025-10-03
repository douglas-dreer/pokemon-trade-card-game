package br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.repository

import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * Repository interface for managing `SerieEntity` persistence.
 * Extends the Spring Data JPA `JpaRepository` to provide CRUD operations
 * and additional custom queries for the `SerieEntity` type.
 */
interface SeriesSpringRepository: JpaRepository<SerieEntity, UUID> {
    /**
     * Retrieves a series entity based on its unique code.
     *
     * @param code The unique code of the series to be retrieved.
     * @return The corresponding [SerieEntity] if found, or `null` otherwise.
     */
    fun findSerieEntityByCode(code: String): SerieEntity?

    /**
     * Checks if a series with the provided unique code already exists in the repository.
     *
     * @param code The unique code of the series to check for existence.
     * @return true if a series with the given code exists, otherwise false.
     */
    fun existsSerieByCode(code: String): Boolean

    /**
     * Checks if a series with the specified name already exists in the repository.
     *
     * @param name The name of the series to check for existence.
     * @return True if a series with the same name exists, otherwise false.
     */
    fun existsSerieByName(name: String): Boolean

}