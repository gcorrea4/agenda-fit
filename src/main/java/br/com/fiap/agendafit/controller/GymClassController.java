package br.com.fiap.agendafit.controller;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.assembler.GymClassModelAssembler;
import br.com.fiap.agendafit.dto.ErrorResponse;
import br.com.fiap.agendafit.dto.GymClassRequest;
import br.com.fiap.agendafit.dto.GymClassResponse;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.Level;
import br.com.fiap.agendafit.model.Modality;
import br.com.fiap.agendafit.service.GymClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
@Tag(name = "Aulas", description = "Grade de aulas: listagem paginada, buscas e gerenciamento pelo ADMIN")
public class GymClassController {

    private final GymClassService service;
    private final GymClassModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Lista as aulas paginadas, ordenadas por horário (10 por página)")
    @ApiResponse(responseCode = "200", description = "Página de aulas")
    public PagedModel<EntityModel<GymClassResponse>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "startsAt") Pageable pageable) {
        return assembler.toPagedModel(service.findAll(pageable));
    }

    @GetMapping("/example")
    @Operation(summary = "Busca aulas iguais a uma aula de exemplo (Query by Example)")
    @ApiResponse(responseCode = "200", description = "Página de aulas")
    @ApiResponse(responseCode = "400", description = "Filtro inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PagedModel<EntityModel<GymClassResponse>> findByExample(
            @Parameter(description = "Modalidade exata") @RequestParam(required = false) Modality modality,
            @Parameter(description = "Nível exato") @RequestParam(required = false) Level level,
            @Parameter(description = "Nome completo do instrutor, sem diferenciar maiúsculas e minúsculas", example = "carla mendes")
            @RequestParam(required = false) String instructor,
            @ParameterObject @PageableDefault(size = 10, sort = "startsAt") Pageable pageable) {
        return assembler.toPagedModel(service.findByExample(modality, level, instructor, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Busca aulas por faixa de horário, trecho do título e vagas (Specification)")
    @ApiResponse(responseCode = "200", description = "Página de aulas")
    @ApiResponse(responseCode = "400", description = "Filtro inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PagedModel<EntityModel<GymClassResponse>> search(
            @Parameter(description = "Trecho do título, sem diferenciar maiúsculas e minúsculas", example = "yoga")
            @RequestParam(required = false) String title,
            @Parameter(description = "Modalidade exata") @RequestParam(required = false) Modality modality,
            @Parameter(description = "Nível exato") @RequestParam(required = false) Level level,
            @Parameter(description = "Aulas que começam em ou depois deste horário (ISO)", example = "2026-11-03T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAfter,
            @Parameter(description = "Aulas que começam em ou antes deste horário (ISO)", example = "2026-11-05T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsBefore,
            @Parameter(description = "true: só aulas com vaga (inscritos < capacity)")
            @RequestParam(required = false) Boolean availableOnly,
            @ParameterObject @PageableDefault(size = 10, sort = "startsAt") Pageable pageable) {
        return assembler.toPagedModel(
                service.search(title, modality, level, startsAfter, startsBefore, availableOnly, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma aula, com os links das ações permitidas ao usuário")
    @ApiResponse(responseCode = "200", description = "Aula")
    @ApiResponse(responseCode = "404", description = "Aula não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public EntityModel<GymClassResponse> findById(@PathVariable Long id) {
        return assembler.toModel(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma aula (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Aula criada, com o header Location")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Usuário não é ADMIN", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<EntityModel<GymClassResponse>> create(@RequestBody @Valid GymClassRequest request) {
        GymClass gymClass = service.create(request);
        EntityModel<GymClassResponse> model = assembler.toModel(gymClass);
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma aula (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Aula atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Usuário não é ADMIN", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aula não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public EntityModel<GymClassResponse> update(@PathVariable Long id, @RequestBody @Valid GymClassRequest request) {
        return assembler.toModel(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma aula e as inscrições dela (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Aula excluída")
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Usuário não é ADMIN", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aula não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
