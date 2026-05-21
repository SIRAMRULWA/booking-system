package com.booking.bookingsystem.repository;

import com.booking.bookingsystem.entity.Resource;
import com.booking.bookingsystem.enums.ResourceStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Optional<Resource> findByResourceCode(String resourceCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Resource r where r.id = :id")
    Optional<Resource> findByIdWithLock(@Param("id") Long id);

    Page<Resource> findByCategoryAndStatus(String category, ResourceStatus status, Pageable pageable);

    // For FLEXIBLE mode: check if resource is free during time range
    @Query("select r from Resource r where r.mode = 'FLEXIBLE' and r.status = 'AVAILABLE' and not exists (" +
            "select b from Booking b where b.resource = r and b.status not in ('CANCELLED', 'COMPLETED') " +
            "and b.startTime < :endTime and b.endTime > :startTime)")
    List<Resource> findAvailableFlexibleResourcesForTimes(@Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    // For FIXED mode: find resources that match the exact date and time
    @Query("select r from Resource r where r.mode = 'FIXED' and r.slotDate = :date and r.status = 'AVAILABLE' " +
            "and r.slotStart = :startTime and r.slotEnd = :endTime " +
            "and not exists (select b from Booking b where b.resource = r and b.status not in ('CANCELLED', 'COMPLETED'))")
    List<Resource> findAvailableFixedSlotResources(@Param("date") LocalDate date,
                                                   @Param("startTime") LocalTime startTime,
                                                   @Param("endTime") LocalTime endTime);

    @Query(
        value = """
            select r from Resource r
            where r.status = 'AVAILABLE'
              and (
                (
                  r.mode = 'FLEXIBLE'
                  and not exists (
                    select b from Booking b
                    where b.resource = r
                      and b.status not in ('CANCELLED', 'COMPLETED')
                      and b.startTime < :endTime
                      and b.endTime > :startTime
                  )
                )
                or
                (
                  r.mode = 'FIXED'
                  and r.slotDate = :date
                  and r.slotStart = :startTimeOnly
                  and r.slotEnd = :endTimeOnly
                  and not exists (
                    select b from Booking b
                    where b.resource = r
                      and b.status not in ('CANCELLED', 'COMPLETED')
                  )
                )
              )
            """,
        countQuery = """
            select count(r) from Resource r
            where r.status = 'AVAILABLE'
              and (
                (
                  r.mode = 'FLEXIBLE'
                  and not exists (
                    select b from Booking b
                    where b.resource = r
                      and b.status not in ('CANCELLED', 'COMPLETED')
                      and b.startTime < :endTime
                      and b.endTime > :startTime
                  )
                )
                or
                (
                  r.mode = 'FIXED'
                  and r.slotDate = :date
                  and r.slotStart = :startTimeOnly
                  and r.slotEnd = :endTimeOnly
                  and not exists (
                    select b from Booking b
                    where b.resource = r
                      and b.status not in ('CANCELLED', 'COMPLETED')
                  )
                )
              )
            """
    )
    Page<Resource> findAvailableResources(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("date") LocalDate date,
                                          @Param("startTimeOnly") LocalTime startTimeOnly,
                                          @Param("endTimeOnly") LocalTime endTimeOnly,
                                          Pageable pageable);

    // Get all FIXED resources for a specific date
    @Query("select r from Resource r where r.mode = 'FIXED' and r.slotDate = :date and r.status = 'AVAILABLE'")
    Page<Resource> findFixedResourcesByDate(@Param("date") LocalDate date, Pageable pageable);

    long countByStatus(ResourceStatus status);
}
