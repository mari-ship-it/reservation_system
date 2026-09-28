package mar.sirenko.reservation_system.reservation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    @Modifying
    @Query("""
            update ReservationEntity r
            set r.status = :status
            where r.id = :id
            """)
    void setStatus(
            @Param("id") Long id,
            @Param("status") ReservationStatus reservationStatus
    );

    @Query("""
            SELECT r.id FROM ReservationEntity r
            WHERE r.roomId = :roomId
            AND :startDte < r.endDate
            AND r.startDate < :endDate
            AND r.status = :status
            """)
    List<Long> findConflictReservationIds(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ReservationStatus status
    );

    @Query("""
            SELECT r FROM ReservationEntity r
            WHERE (:userId IS NULL OR r.userId = :userId)
            AND (:roomId IS NULL OR r.roomId = :roomId)
            """)
    List<ReservationEntity> searchAllByFilter(
            Long userId,
            Long roomId,
            Pageable pageable
    );
}
