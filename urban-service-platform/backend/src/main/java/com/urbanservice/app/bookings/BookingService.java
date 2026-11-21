package com.urbanservice.app.bookings;

import com.urbanservice.app.auth.CurrentUser;
import com.urbanservice.app.catalog.ServicePackage;
import com.urbanservice.app.catalog.ServicePackageRepository;
import com.urbanservice.app.users.*;
import jakarta.validation.constraints.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    public record CreateBookingRequest(
        @NotNull Long packageId,
        @NotNull Long addressId,
        @NotNull @FutureOrPresent LocalDate scheduledDate,
        @NotBlank String timeSlot
    ) {}

    public record UpdateBookingStatusRequest(
        @NotNull Booking.Status status,
        @NotNull Long version,
        Long providerId // Optional, used by ADMIN when assigning
    ) {}

    public record BookingResponse(
        Long id,
        String packageTitle,
        String categoryName,
        BigDecimal totalAmount,
        LocalDate scheduledDate,
        String timeSlot,
        Booking.Status status,
        Long version,
        String customerName,
        String providerName,
        String fullAddress
    ) {
        public static BookingResponse from(Booking b) {
            String addr = b.getAddress().getLabel() + ": " + b.getAddress().getAddressLine() + ", " + b.getAddress().getCity() + " - " + b.getAddress().getPincode();
            return new BookingResponse(
                b.getId(),
                b.getServicePackage().getTitle(),
                b.getServicePackage().getCategory().getName(),
                b.getTotalAmount(),
                b.getScheduledDate(),
                b.getTimeSlot(),
                b.getStatus(),
                b.getVersion(),
                b.getCustomer().getFullName(),
                b.getProvider() != null ? b.getProvider().getFullName() : "Unassigned",
                addr
            );
        }
    }

    private final BookingRepository bookings;
    private final ServicePackageRepository packages;
    private final CustomerAddressRepository addresses;
    private final UserRepository users;
    private final CurrentUser currentUser;

    public BookingService(BookingRepository bookings, ServicePackageRepository packages,
                          CustomerAddressRepository addresses, UserRepository users, CurrentUser currentUser) {
        this.bookings = bookings;
        this.packages = packages;
        this.addresses = addresses;
        this.users = users;
        this.currentUser = currentUser;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest req) {
        User customer = currentUser.requireActive();
        if (customer.getRole() != User.Role.CUSTOMER) {
            throw new AccessDeniedException("Only customers can book services");
        }

        ServicePackage pkg = packages.findById(req.packageId())
            .filter(ServicePackage::isActive)
            .orElseThrow(() -> new IllegalArgumentException("Service package not found or inactive"));

        CustomerAddress addr = addresses.findById(req.addressId())
            .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        // Object-level ownership verification
        if (!addr.getUser().getId().equals(customer.getId())) {
            throw new AccessDeniedException("Address does not belong to current customer");
        }

        Booking b = new Booking();
        b.setCustomer(customer);
        b.setServicePackage(pkg);
        b.setAddress(addr);
        b.setScheduledDate(req.scheduledDate());
        b.setTimeSlot(req.timeSlot().trim());
        b.setTotalAmount(pkg.getPrice()); // Server-owned pricing
        b.setStatus(Booking.Status.PENDING);

        return BookingResponse.from(bookings.saveAndFlush(b));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listScopedBookings() {
        User actor = currentUser.requireActive();
        List<Booking> list = switch (actor.getRole()) {
            case ADMIN -> bookings.findAllByOrderByScheduledDateDescIdDesc();
            case CUSTOMER -> bookings.findByCustomerIdOrderByScheduledDateDescIdDesc(actor.getId());
            case PROVIDER -> bookings.findByProviderIdOrStatusOrderByScheduledDateAsc(actor.getId(), Booking.Status.PENDING);
        };
        return list.stream().map(BookingResponse::from).toList();
    }

    @Transactional
    public BookingResponse transitionStatus(Long bookingId, UpdateBookingStatusRequest req) {
        User actor = currentUser.requireActive();
        Booking b = bookings.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!b.getVersion().equals(req.version())) {
            throw new IllegalStateException("Booking was updated by someone else. Please refresh.");
        }

        Booking.Status next = req.status();
        switch (actor.getRole()) {
            case CUSTOMER -> {
                if (!b.getCustomer().getId().equals(actor.getId())) {
                    throw new AccessDeniedException("Not your booking");
                }
                if (next != Booking.Status.CANCELLED || (b.getStatus() != Booking.Status.PENDING && b.getStatus() != Booking.Status.ASSIGNED)) {
                    throw new IllegalStateException("Customers can only cancel PENDING or ASSIGNED bookings");
                }
            }
            case PROVIDER -> {
                if (next == Booking.Status.ASSIGNED && b.getStatus() == Booking.Status.PENDING && b.getProvider() == null) {
                    ensureProviderSlotFree(actor.getId(), b.getScheduledDate(), b.getTimeSlot());
                    b.setProvider(actor);
                } else {
                    if (b.getProvider() == null || !b.getProvider().getId().equals(actor.getId())) {
                        throw new AccessDeniedException("Job is not assigned to you");
                    }
                    validateProviderTransition(b.getStatus(), next);
                }
            }
            case ADMIN -> {
                if (next == Booking.Status.ASSIGNED && req.providerId() != null) {
                    User provider = users.findById(req.providerId())
                        .filter(u -> u.getRole() == User.Role.PROVIDER && u.getStatus() == User.Status.ACTIVE)
                        .orElseThrow(() -> new IllegalArgumentException("Valid active provider required"));
                    ensureProviderSlotFree(provider.getId(), b.getScheduledDate(), b.getTimeSlot());
                    b.setProvider(provider);
                }
            }
        }

        b.setStatus(next);
        return BookingResponse.from(bookings.saveAndFlush(b));
    }

    private void ensureProviderSlotFree(Long providerId, LocalDate date, String slot) {
        boolean busy = bookings.existsByProviderIdAndScheduledDateAndTimeSlotAndStatusNot(
            providerId, date, slot, Booking.Status.CANCELLED
        );
        if (busy) {
            throw new IllegalStateException("Provider is already booked for this date and time slot");
        }
    }

    private void validateProviderTransition(Booking.Status current, Booking.Status next) {
        boolean valid = (current == Booking.Status.ASSIGNED && next == Booking.Status.IN_PROGRESS)
                     || (current == Booking.Status.IN_PROGRESS && next == Booking.Status.COMPLETED);
        if (!valid) {
            throw new IllegalStateException("Invalid transition from " + current + " to " + next);
        }
    }
}