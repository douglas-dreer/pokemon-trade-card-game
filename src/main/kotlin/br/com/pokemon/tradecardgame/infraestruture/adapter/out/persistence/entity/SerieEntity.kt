package br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity

import br.com.pokemon.tradecardgame.domain.model.Serie
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

/**
 * Represents the database entity for a collectible card series.
 *
 * This class is used to persist and retrieve series-related data from the database. It encapsulates
 * the properties and mappings required for the `series` table, including unique constraints,
 * column configurations, and timestamps for tracking entity creation and updates. It also includes
 * functionality for transforming the entity into its corresponding domain model representation.
 *
 * @property id The unique identifier for the series, generated as a UUID.
 * @property code A unique code representing the series, constrained to a maximum of 10 characters.
 * @property name The name of the series, constrained to a maximum of 100 characters.
 * @property releaseYear The year when the series was released, constrained to 4 digits.
 * @property imageUrl An optional URL associated with the image resource for the series, constrained to a maximum of 255 characters.
 * @property expansions Additional information about expansions related to the series, constrained to a maximum of 255 characters.
 * @property createdAt The timestamp indicating when the entity was created, automatically populated and non-updatable.
 * @property updatedAt The timestamp indicating when the entity was last updated, automatically populated upon updates.
 */
@Entity
@Table(name = "series")
data class SerieEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @SequenceGenerator(name = "uuid", sequenceName = "serie_id_seq")
    val id: UUID?,

    @Column(name = "code", nullable = false, unique = true, length = 10)
    val code: String,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "release_year", nullable = false, length = 4)
    val releaseYear: Int,

    @Column(name = "imageUrl", nullable = true, length = 255)
    val imageUrl: String? = null,

    @Column(name = "expansions", nullable = true, length = 255)
    val expansions: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime? = LocalDateTime.now()
) {
    /**
     * Maps the current [SerieEntity] instance to its corresponding domain model [Serie].
     *
     * Converts the database entity representation of a card series into a domain model instance,
     * facilitating the transfer of data from the persistence layer to the domain layer. This method
     * excludes properties specific to persistence, such as database associations, and ensures that
     * the resulting domain object adheres to the design of the domain model.
     *
     * @return A [Serie] domain model object populated with the data from this [SerieEntity].
     */
    fun toDomain(): Serie = Serie(
        id = this.id,
        code = this.code,
        name = this.name,
        releaseYear = this.releaseYear,
        imageUrl = this.imageUrl,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
