package br.com.pokemon.tradecardgame.application.validation

import org.springframework.beans.factory.annotation.Qualifier

/**
 * Custom qualifier annotation used to mark beans or parameters related to
 * the deletion validation process. This annotation is designed to
 * differentiate and inject validation strategies or components
 * specifically tailored for validating objects during their deletion phase.
 *
 * This annotation can be applied at the class or parameter level.
 *
 * Utilizing the `@Qualifier` annotation, components annotated with
 * `DeleteValidation` are automatically recognized and injected by the
 * Spring framework.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DeleteValidation