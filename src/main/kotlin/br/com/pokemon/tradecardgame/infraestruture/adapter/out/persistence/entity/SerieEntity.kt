package br.com.pokemon.tradecardgame.infraestruture.adapter.out.persistence.entity

import br.com.pokemon.tradecardgame.domain.model.Serie
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

/**
 * Entity class representing a card series in the persistence layer.
 *
 * Maps to the "series" table in the database and holds information about
 * a collectible card series, including its unique identifier, code, name,
 * release year, associated expansions, and metadata such as creation and
 * update timestamps. Used to interact with the database and bridge the gap
 * between the domain and persistence layers.
 *
 * @property id The unique identifier for the series, generated as a UUID.
 * @property code A unique string code representing the series, must be non-null and unique.
 * @property name The name of the series, must be non-null.
 * @property releaseYear The release year of the series, must be non-null.
 * @property imageUrl An optional URL pointing to an image representing the series.
 * @property expansions An optional string representation of expansions related to the series.
 * @property createdAt A timestamp indicating when the record was created, generated automatically.
 * @property updatedAt A timestamp indicating the last time the record was updated, generated automatically.
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
