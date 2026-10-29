package br.com.fiap.agendafit.dto;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.Level;
import br.com.fiap.agendafit.model.Modality;
import org.springframework.hateoas.server.core.Relation;

// Aula como sai na API: a entidade mais as vagas restantes. Nas páginas fica em _embedded.classes
@Relation(itemRelation = "class", collectionRelation = "classes")
public record GymClassResponse(
        Long id,
        String title,
        Modality modality,
        Level level,
        String instructor,
        LocalDateTime startsAt,
        Integer durationMinutes,
        Integer capacity,
        int availableSpots
) {

    public static GymClassResponse of(GymClass gymClass, long enrolled) {
        return new GymClassResponse(
                gymClass.getId(),
                gymClass.getTitle(),
                gymClass.getModality(),
                gymClass.getLevel(),
                gymClass.getInstructor(),
                gymClass.getStartsAt(),
                gymClass.getDurationMinutes(),
                gymClass.getCapacity(),
                (int) Math.max(0, gymClass.getCapacity() - enrolled));
    }
}
