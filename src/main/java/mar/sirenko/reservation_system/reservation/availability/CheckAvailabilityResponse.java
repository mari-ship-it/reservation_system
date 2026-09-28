package mar.sirenko.reservation_system.reservation.availability;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus status
) {
}
