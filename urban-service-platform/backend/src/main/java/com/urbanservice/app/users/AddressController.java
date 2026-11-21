package com.urbanservice.app.users;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbanservice.app.auth.CurrentUser;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/customer/addresses")
@PreAuthorize("hasRole('CUSTOMER')")
public class AddressController {

    public record AddressRequest(
        @NotBlank @Size(max = 40) String label,
        @NotBlank @Size(max = 250) String addressLine,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Size(max = 15) String pincode
    ) {}

    public record AddressResponse(Long id, String label, String addressLine, String city, String pincode) {
        public static AddressResponse from(CustomerAddress a) {
            return new AddressResponse(a.getId(), a.getLabel(), a.getAddressLine(), a.getCity(), a.getPincode());
        }
    }

    private final CustomerAddressRepository addresses;
    private final CurrentUser currentUser;

    public AddressController(CustomerAddressRepository addresses, CurrentUser currentUser) {
        this.addresses = addresses;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<AddressResponse> listMine() {
        User me = currentUser.requireActive();
        return addresses.findByUserIdOrderByIdDesc(me.getId()).stream().map(AddressResponse::from).toList();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<AddressResponse> add(@Valid @RequestBody AddressRequest req) {
        User me = currentUser.requireActive();
        CustomerAddress a = new CustomerAddress();
        a.setUser(me);
        a.setLabel(req.label().trim());
        a.setAddressLine(req.addressLine().trim());
        a.setCity(req.city().trim());
        a.setPincode(req.pincode().trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(AddressResponse.from(addresses.save(a)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User me = currentUser.requireActive();
        CustomerAddress a = addresses.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (!a.getUser().getId().equals(me.getId())) {
            throw new AccessDeniedException("Cannot delete another customer's address");
        }
        addresses.delete(a);
        return ResponseEntity.noContent().build();
    }
}