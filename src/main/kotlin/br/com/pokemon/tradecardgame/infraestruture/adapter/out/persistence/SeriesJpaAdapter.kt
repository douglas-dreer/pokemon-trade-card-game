package br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence

import br.com.pokemon.tradecardgame.domain.port.SerieRepositoryPort
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity.SerieEntity
import br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.repository.SeriesSpringRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.util.*


/**
 * Adapter class implementing the [SerieRepositoryPort] interface to manage
 * data persistence and retrieval for `SerieEntity` using a JPA-based repository.
 * Integrates the [SeriesSpringRepository] for database operations and serves
 * as a bridge between the application domain and the underlying data storage.
 *
 * This implementation follows the Hexagonal Architecture pattern by acting
 * as a secondary adapter that connects the domain layer with the infrastructure layer.
 *
 * @property springRepository The JPA-based repository for performing database-level
 * operations on `SerieEntity` instances.
 */
@Repository
class SeriesJpaAdapter(
    private val springRepository: SeriesSpringRepository
) : SerieRepositoryPort {
    /**
     * Retrieves a paginated list of all series from the repository.
     *
     * @param page The page number to retrieve, starting from 0.
     * @param pageSize The number of items to include per page.
     * @return A [Page] containing the paginated list of series as [SerieEntity] instances.
     */
    override fun findAllSeries(
        page: Int,
        pageSize: Int
    ): Page<SerieEntity> {
        val pageable = PageRequest.of(page, pageSize)
        return springRepository.findAll(pageable)
    }

    /**
     * Retrieves a series entity by its unique identifier.
     *
     * @param serieId The unique identifier of the series to be retrieved.
     * @return The [SerieEntity] if found, or null otherwise.
     */
    override fun findSerieById(serieId: UUID): SerieEntity? {
        return springRepository.findById(serieId).orElse(null)
    }


    /**
     * Finds a series entity based on its unique code.
     *
     * @param code The unique code of the series to be retrieved.
     * @return The corresponding [SerieEntity] if found, or null otherwise.
     */
    override fun findSerieByCode(code: String): SerieEntity? {
        return springRepository.findSerieEntityByCode(code)
    }

    /**
     * Creates a new series entity and persists it in the repository.
     *
     * @param serieEntity The [SerieEntity] object containing the data of the series to be created.
     * @return The persisted [SerieEntity] object after successful creation.
     */
    override fun createSerie(serieEntity: SerieEntity): SerieEntity {
        return springRepository.save(serieEntity)
    }

    /**
     * Updates the details of an existing series entity in the repository.
     *
     * @param serieEntity The [SerieEntity] instance containing the updated details of the series.
     * @return The updated [SerieEntity] instance after persisting the changes in the repository.
     */
    override fun updateSerie(serieEntity: SerieEntity): SerieEntity {
        return springRepository.save(serieEntity)
    }

    /**
     * Deletes a series by its unique identifier.
     *
     * @param serieId The unique identifier of the series to be deleted.
     */
    override fun deleteSerieById(serieId: UUID) {
        springRepository.deleteById(serieId)
    }

    /**
     * Checks if a series exists with the given unique identifier.
     *
     * @param serieId The unique identifier of the series to check for existence.
     * @return `true` if a series with the specified identifier exists, otherwise `false`.
     */
    override fun existsSerieById(serieId: UUID): Boolean {
        return springRepository.existsById(serieId)
    }

    /**
     * Checks if there is an existing series with the same code in the repository.
     *
     */
    override fun existsSerieByCode(code: String): Boolean {
        return springRepository.existsSerieByCode(code)
    }

    /**
     * Checks if a series with the specified name already exists in the repository.
     *
     * @param name The name of the series to check for existence.
     * @return True if a series with the same name exists, otherwise false.
     */
    override fun existsSerieByName(name: String): Boolean {
        return springRepository.existsSerieByName(name)
    }
}
