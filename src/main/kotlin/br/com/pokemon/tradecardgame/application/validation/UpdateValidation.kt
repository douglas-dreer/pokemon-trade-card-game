package br.com.pokemon.tradecardgame.application.validation

import org.springframework.beans.factory.annotation.Qualifier

/**
 * Custom qualifier annotation used to mark beans or parameters related to
 * the update validation process. This annotation is primarily utilized to
 * distinguish and inject validation strategies or components specifically
 * designed for validating objects during their update phase.
 *
 * This annotation can be applied to both classes and value parameters.
 *
 * By leveraging the `@Qualifier` annotation, components annotated with
 * `UpdateValidation` are automatically recognized and injected by the
 * Spring framework.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class UpdateValidation {
}