package br.com.tcg.pokemon.pokemontradecardgame.infraestruture.adapter.web.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/cards")
class CardController {
    @GetMapping
    fun listCards(): ResponseEntity<String> {
        return ResponseEntity.ok("Cards")
    }

}
