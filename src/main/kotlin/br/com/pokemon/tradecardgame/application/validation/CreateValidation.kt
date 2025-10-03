package br.com.pokemon.tradecardgame.application.validation

import org.springframework.beans.factory.annotation.Qualifier

/**
 * Custom qualifier annotation used to mark beans or parameters related to
 * the creation validation process. This annotation is primarily used to
 * differentiate and inject validation strategies or components
 * specifically designed for validating objects during their creation phase.
 *
 * This annotation can be applied at the class or value parameter level.
 *
 * The annotated components are automatically recognized and injected by the
 * Spring framework due to the use of the `@Qualifier` annotation.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class CreateValidation