package br.com.pokemon.tradecardgame.application.adapter.`in`

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cards")
class CardController {
    @GetMapping
    fun listCards(): ResponseEntity<String> {
        return ResponseEntity.ok("Cards")
    }

}