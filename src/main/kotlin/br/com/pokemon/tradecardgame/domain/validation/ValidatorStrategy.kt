package br.com.pokemon.tradecardgame.domain.validation

/**
 * Interface base para validadores de regra de negócio.
 * T é o modelo de domínio que está sendo validado (ex: Series).
 */
interface ValidatorStrategy<T> {
    fun execute(item: T)
}