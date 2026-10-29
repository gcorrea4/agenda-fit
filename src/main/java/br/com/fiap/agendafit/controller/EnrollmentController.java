package br.com.fiap.agendafit.controller;

import br.com.fiap.agendafit.assembler.EnrollmentModelAssembler;
import br.com.fiap.agendafit.dto.EnrollmentResponse;
import br.com.fiap.agendafit.dto.ErrorResponse;
import br.com.fiap.agendafit.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Inscrições", description = "Inscrição nas aulas; cada aluno só mexe nas próprias inscrições")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService service;
    private final EnrollmentModelAssembler assembler;

    @PostMapping("/classes/{id}/enrollments")
    @Operation(summary = "Inscreve o usuário logado na aula")
    @ApiResponse(responseCode = "201", description = "Inscrição criada")
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aula não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Aula lotada ou usuário já inscrito", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<EntityModel<EnrollmentResponse>> enroll(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(service.enroll(id)));
    }

    @GetMapping("/enrollments/me")
    @Operation(summary = "Lista as inscrições do usuário logado, paginadas por horário da aula")
    @ApiResponse(responseCode = "200", description = "Página de inscrições")
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PagedModel<EntityModel<EnrollmentResponse>> findMine(
            @ParameterObject @PageableDefault(size = 10, sort = "gymClass.startsAt") Pageable pageable) {
        return assembler.toPagedModel(service.findMine(pageable));
    }

    @DeleteMapping("/enrollments/{id}")
    @Operation(summary = "Cancela uma inscrição (dono ou ADMIN)")
    @ApiResponse(responseCode = "204", description = "Inscrição cancelada")
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "A inscrição é de outro usuário", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Inscrição não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
