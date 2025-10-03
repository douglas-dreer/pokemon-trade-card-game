package br.com.pokemon.tradecardgame.domain.enums

/**
 * Represents the rarity type associated with collectible items, such as trading cards,
 * and provides descriptive labels for each rarity in both English and Brazilian Portuguese.
 *
 * This enum is commonly used across the domain layer to define the rarity classification
 * of items. It includes various rarity levels ranging from standard to ultra-rare types,
 * as well as special illustration-based classifications. Each enum constant holds a
 * description in English and its equivalent in Portuguese.
 *
 * @property description The description of the rarity in English.
 * @property descriptionPtBR The description of the rarity in Brazilian Portuguese.
 */
enum class RarityType(
    val description: String,
    val descriptionPtBR: String
) {
    STANDARD("standard", "Cartas Padrão"),
    FOIL("standard set foil", "Cartas Holográfica Padrão"),
    COMMON("common", "Comum"),
    UNCOMMON("uncommon", "Incomum"),
    RARE("rare", "Rara"),
    DOUBLE_RARE("double rare", "Rara Dupla"),
    ULTRA_RARE("ultra rare", "Ultra Rara"),
    ILLUSTRATION_RARE("illustration rare", "Ilustração Rara"),
    SPECIAL_ILLUSTRATION_RARE("special illustration rare", "Ilustração Rara Especial"),
    HYPER_RARE("Hyper Rare", "Ultra Rara")
}