package br.com.fiap.agendafit.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import br.com.fiap.agendafit.controller.EnrollmentController;
import br.com.fiap.agendafit.controller.GymClassController;
import br.com.fiap.agendafit.dto.EnrollmentResponse;
import br.com.fiap.agendafit.model.Enrollment;
import br.com.fiap.agendafit.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

// Inscrição com o link da aula e o cancel só para o dono ou o ADMIN
@Component
@RequiredArgsConstructor
public class EnrollmentModelAssembler implements RepresentationModelAssembler<Enrollment, EntityModel<EnrollmentResponse>> {

    private final EnrollmentService enrollmentService;
    private final PagedResourcesAssembler<Enrollment> pagedResourcesAssembler;

    @Override
    public EntityModel<EnrollmentResponse> toModel(Enrollment enrollment) {
        EntityModel<EnrollmentResponse> model = EntityModel.of(EnrollmentResponse.of(enrollment),
                linkTo(methodOn(GymClassController.class).findById(enrollment.getGymClass().getId())).withRel("class"));
        if (enrollmentService.canCancel(enrollment)) {
            model.add(linkTo(methodOn(EnrollmentController.class).cancel(enrollment.getId())).withRel("cancel"));
        }
        return model;
    }

    public PagedModel<EntityModel<EnrollmentResponse>> toPagedModel(Page<Enrollment> page) {
        return pagedResourcesAssembler.toModel(page, this);
    }
}
