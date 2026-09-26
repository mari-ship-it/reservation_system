package mar.sirenko.reservation_system;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReservations() {
        log.info("Called getAllReservation");
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.findAllReservation());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable("id") Long id) {

        log.info("Called getReservationById: id ={}", id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationById(id));
    }

    @PostMapping()
    public ResponseEntity<Reservation> createReservation(@RequestBody @Valid Reservation reservationToCreate) {

        log.info("Called createReservation");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id, @RequestBody @Valid Reservation reservationToUpdate) {

        log.info("Called updateReservation: id={}, reservationToUpdate={}", id, reservationToUpdate);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.updateReservation(id, reservationToUpdate));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {

        log.info("Called deleteReservation: id={}", id);

        reservationService.cancelReservation(id);
        return ResponseEntity.status(HttpStatus.OK).build();

    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id) {

        log.info("Called approveReservation: id={}", id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.approveReservation(id));
    }

}
