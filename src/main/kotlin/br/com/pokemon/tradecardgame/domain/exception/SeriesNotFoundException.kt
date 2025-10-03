package br.com.pokemon.tradecardgame.domain.exception

import java.util.UUID

/**
 * Indica que uma série com o nome especificado não foi encontrada no sistema.
 *
 * Essa exceção é lançada quando uma tentativa é feita para acessar ou manipular uma
 * série inexistente, impedindo operações em dados que não estão cadastrados no aplicativo.
 *
 * @constructor Cria uma nova [SeriesNotFoundException].
 * @param name Nome da série que não foi encontrada no sistema.
 */
class SeriesNotFoundException(
    id: UUID
): DomainException("The series '$id' was not found in the system.") {
}