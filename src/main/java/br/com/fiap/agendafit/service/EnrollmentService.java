package br.com.fiap.agendafit.service;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.model.Enrollment;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.User;
import br.com.fiap.agendafit.repository.EnrollmentRepository;
import br.com.fiap.agendafit.repository.UserRepository;
import br.com.fiap.agendafit.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository repository;
    private final UserRepository userRepository;
    private final GymClassService gymClassService;
    private final CurrentUser currentUser;

    @Transactional
    public Enrollment enroll(Long classId) {
        GymClass gymClass = gymClassService.findById(classId);
        User user = loggedUser();
        if (repository.existsByUserEmailAndGymClassId(user.getEmail(), classId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled in this class");
        }
        if (repository.countByGymClassId(classId) >= gymClass.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Class is full");
        }
        return repository.save(Enrollment.builder()
                .user(user)
                .gymClass(gymClass)
                .createdAt(LocalDateTime.now())
                .build());
    }

    // o aluno só enxerga as próprias inscrições
    public Page<Enrollment> findMine(Pageable pageable) {
        return repository.findByUserEmail(loggedUser().getEmail(), pageable);
    }

    // o dono cancela a própria inscrição; o ADMIN cancela qualquer uma
    public void cancel(Long id) {
        Enrollment enrollment = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        if (!canCancel(enrollment)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own enrollments");
        }
        repository.delete(enrollment);
    }

    public boolean canCancel(Enrollment enrollment) {
        return currentUser.isAdmin() || currentUser.email()
                .map(email -> email.equals(enrollment.getUser().getEmail()))
                .orElse(false);
    }

    private User loggedUser() {
        return currentUser.email()
                .flatMap(userRepository::findByEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }
}
