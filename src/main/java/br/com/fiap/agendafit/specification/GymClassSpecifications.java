package br.com.fiap.agendafit.specification;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.model.Enrollment;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.Level;
import br.com.fiap.agendafit.model.Modality;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

// Uma Specification por filtro do GET /classes/search; o service combina só as informadas com and
public final class GymClassSpecifications {

    private GymClassSpecifications() {}

    // "contém", sem diferenciar maiúsculas e minúsculas
    public static Specification<GymClass> titleContains(String title) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<GymClass> modalityEquals(Modality modality) {
        return (root, query, cb) -> cb.equal(root.get("modality"), modality);
    }

    public static Specification<GymClass> levelEquals(Level level) {
        return (root, query, cb) -> cb.equal(root.get("level"), level);
    }

    public static Specification<GymClass> startsAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startsAt"), dateTime);
    }

    public static Specification<GymClass> startsBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startsAt"), dateTime);
    }

    // inscritos < capacity, contando as inscrições da aula com uma subquery
    public static Specification<GymClass> hasAvailableSpots() {
        return (root, query, cb) -> {
            Subquery<Long> enrolled = query.subquery(Long.class);
            Root<Enrollment> enrollment = enrolled.from(Enrollment.class);
            enrolled.select(cb.count(enrollment))
                    .where(cb.equal(enrollment.get("gymClass"), root));
            return cb.lt(enrolled, root.<Integer>get("capacity"));
        };
    }
}
