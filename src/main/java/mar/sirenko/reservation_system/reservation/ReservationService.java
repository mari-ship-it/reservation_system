package mar.sirenko.reservation_system.reservation;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import mar.sirenko.reservation_system.reservation.availability.ReservationAvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final ReservationAvailabilityService availabilityService;

    public ReservationService(
            ReservationRepository repository, ReservationMapper mapper,
            ReservationAvailabilityService availabilityService) {
        this.repository = repository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    public Reservation getReservationById(
            Long id
    ) {
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found reservation by id = " + id
                ));
        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter
    ) {
        int pageSize = filter.pageSize() != null
                ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null
                ? filter.pageNumber() : 0;

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.userId(),
                filter.roomId(),
                pageable
        );
        return allEntities.stream()
                .map(mapper::toDomain
                ).toList();
    }

    public Reservation createReservation(
            Reservation reservationToCreate
    ) {

        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day earlier then and date");
        }
        ReservationEntity entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);

        ReservationEntity savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    public Reservation updateReservation(
            Long id, Reservation reservationToUpdate
    ) {
        ReservationEntity reservationEntity = repository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status = " + reservationEntity.getStatus());
        }
        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day earlier then and date");
        }
        ReservationEntity entityToUpdate = mapper.toEntity(reservationToUpdate);
        entityToUpdate.setId(reservationEntity.getId());
        entityToUpdate.setStatus(ReservationStatus.PENDING);

        ReservationEntity updatedEntity = repository.save(entityToUpdate);
        return mapper.toDomain(updatedEntity);
    }

    @Transactional
    public void cancelReservation(
            Long id
    ) {
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getStatus() == ReservationStatus.APPROVED) {
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager please");
        }

        if (reservationEntity.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel the reservation. Reservation was already cancelled");
        }

        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }

    public Reservation approveReservation(
            Long id
    ) {
        ReservationEntity reservationEntity = repository
                .findById(id).orElseThrow(() ->
                        new EntityNotFoundException("Not found reservation by id = " + id));
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation: status = " + reservationEntity.getStatus());
        }
        var isAvailableApprove = availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );
        if (isAvailableApprove) {
            throw new IllegalStateException("Cannot approve reservation because of conflict");
        }
        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }

}
