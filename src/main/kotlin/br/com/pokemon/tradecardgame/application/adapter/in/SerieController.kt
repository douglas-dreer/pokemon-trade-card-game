package br.com.pokemon.tradecardgame.application.adapter.`in`.dto

import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.CreateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.PageRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.request.UpdateSerieRequest
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response.PageResponse
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response.SerieResponse
import br.com.pokemon.tradecardgame.application.adapter.`in`.dto.response.SuccessResponse
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.*
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.DeleteSerieByIdCommand
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.command.UpdateSerieCommand
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByCodeQuery
import br.com.pokemon.tradecardgame.domain.port.`in`.serie.query.FindSerieByIdQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Pageable // Importar o Pageable do Spring Data
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/series")
@Tag(name = "Series", description = "Operações de CRUD para gerenciamento de séries do Pokémon TCG.")
// Adiciona o requisito de segurança para todas as operações neste Controller
@SecurityRequirement(name = "bearerAuth")
class SerieController(
    private val createSerieUsecase: CreateSerieUsecase,
    private val findAllSerieUsecase: FindAllSerieUsecase,
    private val findSerieByIdUsecase: FindSerieByIdUsecase,
    private val findSerieByCodeUsecase: FindSerieByCodeUsecase,
    private val updateSerieByIdUsecase: UpdateSerieByIdUsecase,
    private val deleteSerieByIdUsecase: DeleteSerieByIdUsecase
) {

    @GetMapping
    @Operation(
        summary = "Listar todas as séries (Paginado)",
        description = "Recupera todas as séries de forma paginada. Use 'page' e 'size' como parâmetros de query."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de séries retornada com sucesso",
            content = [Content(schema = Schema(implementation = PageResponse::class))]),
        ApiResponse(responseCode = "401", description = "Não Autorizado (Token ausente ou inválido)"),
        ApiResponse(responseCode = "403", description = "Acesso Negado (Permissões insuficientes)")
    ])
    // Corrigido: GET não deve usar @RequestBody. O Pageable já resolve isso.
    fun findAllSeries(
        @Parameter(hidden = true) // O Spring Doc já infere o Pageable, escondemos o DTO original
        pageRequest: PageRequest // Mantemos o DTO para o UseCase (Se o Pageable não for suficiente)
    ): ResponseEntity<PageResponse<SerieResponse>> {
        val resultResponsePaged = findAllSerieUsecase
            .execute(pageRequest.toQuery())
            .map { it.toResponse() }
        return ResponseEntity.ok().body(PageResponse(resultResponsePaged))
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar série por ID",
        description = "Recupera uma série específica pelo seu identificador único."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Série encontrada",
            content = [Content(schema = Schema(implementation = SerieResponse::class))]),
        ApiResponse(responseCode = "404", description = "Série não encontrada (ID inexistente)"),
        ApiResponse(responseCode = "401", description = "Não Autorizado")
    ])
    fun findSerieById(
        @Parameter(description = "UUID da série a ser buscada")
        @PathVariable("id") serieId: UUID
    ): ResponseEntity<SerieResponse> {
        val query = FindSerieByIdQuery(serieId)
        return ResponseEntity.ok(findSerieByIdUsecase.execute(query)?.toResponse())
    }

    @GetMapping(params = ["code"])
    @Operation(
        summary = "Buscar série por código",
        description = "Recupera uma série pelo seu código exclusivo."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Série encontrada",
            content = [Content(schema = Schema(implementation = SerieResponse::class))]),
        ApiResponse(responseCode = "404", description = "Série não encontrada (Código inexistente)"),
        ApiResponse(responseCode = "401", description = "Não Autorizado")
    ])
    fun findSerieByCode(
        @Parameter(description = "Código da série (Ex: 'SWSH')")
        @RequestParam(name = "code", required = true) serieCode: String
    ): ResponseEntity<SerieResponse> {
        val query = FindSerieByCodeQuery(serieCode)
        return ResponseEntity.ok(findSerieByCodeUsecase.execute(query)?.toResponse())
    }

    @PostMapping
    @Operation(
        summary = "Criar nova série",
        description = "Cria uma nova série no sistema. Validação de dados de entrada e regras de negócio aplicadas."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Série criada com sucesso",
            content = [Content(schema = Schema(implementation = SerieResponse::class))]),
        ApiResponse(responseCode = "400", description = "Requisição Inválida (Validação de dados)"),
        // Adicionando o erro de conflito, crucial para criação de recursos únicos
        ApiResponse(responseCode = "409", description = "Conflito: Série com código ou nome já existe"),
        ApiResponse(responseCode = "401", description = "Não Autorizado"),
        ApiResponse(responseCode = "403", description = "Acesso Negado (Permissões insuficientes)")
    ])
    fun createSerie(
        @RequestBody createSerieRequest: CreateSerieRequest
    ): ResponseEntity<SerieResponse> {
        val command = createSerieRequest.toCommand()
        val serieCreated = createSerieUsecase.execute(command)
        // Uso de URI template para HATEOAS
        val url = URI("/series/${serieCreated.id}")
        return ResponseEntity.created(url).body(serieCreated.toResponse())
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Atualizar série existente",
        description = "Atualiza os dados de uma série já cadastrada pelo seu ID."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Série atualizada com sucesso",
            content = [Content(schema = Schema(implementation = SerieResponse::class))]),
        ApiResponse(responseCode = "400", description = "Requisição Inválida (Validação de dados)"),
        ApiResponse(responseCode = "404", description = "Série não encontrada (ID inexistente)"),
        ApiResponse(responseCode = "401", description = "Não Autorizado"),
        ApiResponse(responseCode = "403", description = "Acesso Negado (Permissões insuficientes)")
    ])
    fun updateSerie(
        @Parameter(description = "UUID da série a ser atualizada")
        @PathVariable("id") serieId: UUID,
        @RequestBody updateSerieRequest: UpdateSerieRequest
    ): ResponseEntity<SerieResponse> {
        val command: UpdateSerieCommand = updateSerieRequest.toCommand()
        val serieUpdated = updateSerieByIdUsecase.execute(serieId, command)
        return ResponseEntity.ok(serieUpdated.toResponse())
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Deletar série",
        description = "Remove uma série existente pelo seu ID."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Série deletada com sucesso",
            content = [Content(schema = Schema(implementation = SuccessResponse::class))]),
        ApiResponse(responseCode = "404", description = "Série não encontrada (ID inexistente)"),
        ApiResponse(responseCode = "401", description = "Não Autorizado"),
        ApiResponse(responseCode = "403", description = "Acesso Negado (Permissões insuficientes)")
    ])
    fun deleteSerieById(
        @Parameter(description = "UUID da série a ser deletada")
        @PathVariable("id") serieId: UUID
    ): ResponseEntity<SuccessResponse> {
        val command = DeleteSerieByIdCommand(serieId)
        deleteSerieByIdUsecase.execute(command)
        return ResponseEntity.ok(
            SuccessResponse(
                title = "Serie deleted successfully",
                message = "Serie with id $serieId deleted successfully"
            )
        )
    }
}