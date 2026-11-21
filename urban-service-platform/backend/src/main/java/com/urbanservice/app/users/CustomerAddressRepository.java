package com.urbanservice.app.users;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByUserIdOrderByIdDesc(Long userId);
}