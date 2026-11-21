package com.urbanservice.app.bookings;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByOrderByScheduledDateDescIdDesc();
    List<Booking> findByCustomerIdOrderByScheduledDateDescIdDesc(Long customerId);
    List<Booking> findByProviderIdOrStatusOrderByScheduledDateAsc(Long providerId, Booking.Status status);
    boolean existsByProviderIdAndScheduledDateAndTimeSlotAndStatusNot(
        Long providerId, LocalDate scheduledDate, String timeSlot, Booking.Status excludedStatus
    );
}