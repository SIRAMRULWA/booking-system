package com.booking.bookingsystem.repository;

import com.booking.bookingsystem.entity.Booking;
import com.booking.bookingsystem.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.resource.id = :resourceId and b.status not in ('CANCELLED', 'COMPLETED') " +
        "and b.startTime < :endTime and b.endTime > :startTime")
    List<Booking> findConflictingBookings(@Param("resourceId") Long resourceId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.status = 'CONFIRMED' and b.startTime between :start and :end")
    List<Booking> findUpcomingStarts(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Modifying
    @Query("update Booking b set b.status = :newStatus where b.id = :bookingId and b.status = :currentStatus")
    int updateBookingStatus(@Param("bookingId") Long bookingId,
                            @Param("currentStatus") BookingStatus currentStatus,
                            @Param("newStatus") BookingStatus newStatus);
}
