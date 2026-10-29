package br.com.fiap.agendafit.dto;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.model.Enrollment;
import org.springframework.hateoas.server.core.Relation;

// Nas páginas fica em _embedded.enrollments
@Relation(itemRelation = "enrollment", collectionRelation = "enrollments")
public record EnrollmentResponse(
        Long id,
        Long classId,
        String classTitle,
        LocalDateTime startsAt,
        LocalDateTime createdAt
) {

    public static EnrollmentResponse of(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getGymClass().getId(),
                enrollment.getGymClass().getTitle(),
                enrollment.getGymClass().getStartsAt(),
                enrollment.getCreatedAt());
    }
}
