package br.com.pokemon.tradecardgame.domain.port

import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import org.springframework.data.domain.Page
import java.util.UUID


/**
 * Interface representing the repository port for managing Serie entities
 * in the context of a Hexagonal Architecture. ProvserieIdes various methods
 * to perform CRUD operations, find operations, and data valserieIdation.
 */
interface SerieRepositoryPort {


    /**
     * Retrieves a paginated list of all series.
     *
     * @param page The page number to retrieve, starting from 0. Defaults to 0.
     * @param pageSize The number of items per page. Defaults to 50.
     * @return A paginated list of series as [Page] containing [SerieEntity].
     */
    fun findAllSeries(page: Int = 0, pageSize: Int = 50): Page<SerieEntity>

    /**
     * Retrieves a series entity based on its unique serieIdentifier.
     *
     * @param serieId The unique serieIdentifier of the series to be retrieved.
     * @return The corresponding [SerieEntity] if found, or null otherwise.
     */
    fun findSerieById(serieId: UUID): SerieEntity?

    /**
     * Finds a series entity based on its unique code.
     *
     * @param code The unique code of the series to be retrieved.
     * @return The corresponding [SerieEntity] if found, or null otherwise.
     */
    fun findSerieByCode(code: String): SerieEntity?

    /**
     * Creates and saves a new series entity in the repository.
     *
     * @param serieEntity The [SerieEntity] instance to be created and saved.
     * @return The newly created [Serie*/
    fun createSerie(serieEntity: SerieEntity): SerieEntity

    /**
     * Updates the details of an existing series entity in the repository.
     *
     * @param serieEntity The [SerieEntity] instance containing the updated details of the series.
     * @return The updated [SerieEntity] instance.
     */
    fun updateSerie(serieEntity: SerieEntity): SerieEntity

    /**
     * Deletes a series entity by its unique serieIdentifier.
     *
     * @param serieId The unique serieIdentifier of the series to be deleted.
     */
    fun deleteSerieById(serieId: UUID)

    /**
     * Checks if a series exists for the given unique serieIdentifier.
     *
     * @param serieId The unique serieIdentifier of the series to check.
     * @return `true` if a series with the specified serieId exists, otherwise `false`.
     */
    fun existsSerieById(serieId: UUID): Boolean

    /**
     * Checks whether a series exists with the provserieIded unique code.
     *
     * @param code The unique code of the series to be checked.
     * @return `true` if a series with the specified code exists, otherwise `false`.
     */
    fun existsSerieByCode(code: String): Boolean

    /**
     * Checks whether a series with the specified name exists in the repository.
     *
     * @param name The name of the series to be checked.
     * @return `true` if a series with the given name exists, otherwise `false`.
     */
    fun existsSerieByName(name: String): Boolean
}
