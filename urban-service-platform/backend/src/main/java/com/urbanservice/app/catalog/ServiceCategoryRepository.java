package com.urbanservice.app.catalog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    List<ServiceCategory> findByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}