package br.com.fiap.agendafit.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import br.com.fiap.agendafit.controller.EnrollmentController;
import br.com.fiap.agendafit.controller.GymClassController;
import br.com.fiap.agendafit.dto.GymClassResponse;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.repository.EnrollmentRepository;
import br.com.fiap.agendafit.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/**
 * Monta a aula com os links que o usuário logado pode seguir.
 * Consulta o SecurityContext a cada requisição: o app só mostra um botão quando o link vem na resposta.
 */
@Component
@RequiredArgsConstructor
public class GymClassModelAssembler implements RepresentationModelAssembler<GymClass, EntityModel<GymClassResponse>> {

    private final EnrollmentRepository enrollmentRepository;
    private final CurrentUser currentUser;
    private final PagedResourcesAssembler<GymClass> pagedResourcesAssembler;

    @Override
    public EntityModel<GymClassResponse> toModel(GymClass gymClass) {
        Long id = gymClass.getId();
        GymClassResponse response = GymClassResponse.of(gymClass, enrollmentRepository.countByGymClassId(id));
        EntityModel<GymClassResponse> model = EntityModel.of(response,
                linkTo(methodOn(GymClassController.class).findById(id)).withSelfRel(),
                linkTo(GymClassController.class).withRel("classes"));

        // enroll: logado, com vaga e ainda não inscrito
        boolean canEnroll = response.availableSpots() > 0 && currentUser.email()
                .map(email -> !enrollmentRepository.existsByUserEmailAndGymClassId(email, id))
                .orElse(false);
        if (canEnroll) {
            model.add(linkTo(methodOn(EnrollmentController.class).enroll(id)).withRel("enroll"));
        }

        // update e delete: só ADMIN
        if (currentUser.isAdmin()) {
            model.add(linkTo(methodOn(GymClassController.class).update(id, null)).withRel("update"));
            model.add(linkTo(methodOn(GymClassController.class).delete(id)).withRel("delete"));
        }
        return model;
    }

    // página de aulas com first/prev/next/last (mantendo os filtros da URL) e o create só para o ADMIN
    public PagedModel<EntityModel<GymClassResponse>> toPagedModel(Page<GymClass> page) {
        PagedModel<EntityModel<GymClassResponse>> model = pagedResourcesAssembler.toModel(page, this);
        if (currentUser.isAdmin()) {
            model.add(linkTo(GymClassController.class).withRel("create"));
        }
        return model;
    }
}
