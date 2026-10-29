package br.com.fiap.agendafit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.fiap.agendafit.dto.GymClassRequest;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.Level;
import br.com.fiap.agendafit.model.Modality;
import br.com.fiap.agendafit.repository.EnrollmentRepository;
import br.com.fiap.agendafit.repository.GymClassRepository;
import br.com.fiap.agendafit.specification.GymClassSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GymClassService {

    private final GymClassRepository repository;
    private final EnrollmentRepository enrollmentRepository;

    public Page<GymClass> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Query by Example: o probe só tem os filtros recebidos; os nulos são ignorados e os textos comparados sem caixa
    public Page<GymClass> findByExample(Modality modality, Level level, String instructor, Pageable pageable) {
        GymClass probe = GymClass.builder()
                .modality(modality)
                .level(level)
                .instructor(StringUtils.hasText(instructor) ? instructor.trim() : null)
                .build();
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        return repository.findAll(Example.of(probe, matcher), pageable);
    }

    // Specification: uma por filtro informado, combinadas com and
    public Page<GymClass> search(String title, Modality modality, Level level, LocalDateTime startsAfter,
                                 LocalDateTime startsBefore, Boolean availableOnly, Pageable pageable) {
        List<Specification<GymClass>> filters = new ArrayList<>();
        if (StringUtils.hasText(title)) filters.add(GymClassSpecifications.titleContains(title.trim()));
        if (modality != null) filters.add(GymClassSpecifications.modalityEquals(modality));
        if (level != null) filters.add(GymClassSpecifications.levelEquals(level));
        if (startsAfter != null) filters.add(GymClassSpecifications.startsAfter(startsAfter));
        if (startsBefore != null) filters.add(GymClassSpecifications.startsBefore(startsBefore));
        if (Boolean.TRUE.equals(availableOnly)) filters.add(GymClassSpecifications.hasAvailableSpots());
        return repository.findAll(Specification.allOf(filters), pageable);
    }

    public GymClass findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }

    public GymClass create(GymClassRequest request) {
        return repository.save(request.toEntity());
    }

    public GymClass update(Long id, GymClassRequest request) {
        findById(id);
        GymClass gymClass = request.toEntity();
        gymClass.setId(id);
        return repository.save(gymClass);
    }

    // apaga as inscrições antes, senão a chave estrangeira impede o delete
    @Transactional
    public void delete(Long id) {
        GymClass gymClass = findById(id);
        enrollmentRepository.deleteByGymClassId(id);
        repository.delete(gymClass);
    }
}
