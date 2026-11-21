package com.urbanservice.app.bookings;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingService.BookingResponse> list() {
        return bookingService.listScopedBookings();
    }

    @PostMapping
    public ResponseEntity<BookingService.BookingResponse> create(@Valid @RequestBody BookingService.CreateBookingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(req));
    }

    @PatchMapping("/{id}/status")
    public BookingService.BookingResponse updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody BookingService.UpdateBookingStatusRequest req
    ) {
        return bookingService.transitionStatus(id, req);
    }
}