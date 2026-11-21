package com.urbanservice.app.catalog;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api")
public class CatalogController {

    public record CategoryRequest(@NotBlank @Size(max = 80) String name, @Size(max = 300) String description) {}
    public record CategoryResponse(Long id, String name, String description, boolean active) {
        public static CategoryResponse from(ServiceCategory c) {
            return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.isActive());
        }
    }

    public record PackageRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 120) String title,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("1.00") BigDecimal price,
        @NotNull @Min(15) @Max(480) Integer durationMinutes
    ) {}

    public record PackageResponse(Long id, Long categoryId, String categoryName, String title, String description, BigDecimal price, Integer durationMinutes) {
        public static PackageResponse from(ServicePackage p) {
            return new PackageResponse(p.getId(), p.getCategory().getId(), p.getCategory().getName(),
                p.getTitle(), p.getDescription(), p.getPrice(), p.getDurationMinutes());
        }
    }

    private final ServiceCategoryRepository categories;
    private final ServicePackageRepository packages;

    public CatalogController(ServiceCategoryRepository categories, ServicePackageRepository packages) {
        this.categories = categories;
        this.packages = packages;
    }

    @GetMapping("/catalog/categories")
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categories.findByActiveTrueOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @GetMapping("/catalog/packages")
    @Transactional(readOnly = true)
    public List<PackageResponse> listPackages() {
        return packages.findByActiveTrueOrderByIdDesc().stream().map(PackageResponse::from).toList();
    }

    @PostMapping("/admin/catalog/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest req) {
        if (categories.existsByNameIgnoreCase(req.name().trim())) {
            throw new IllegalStateException("Category already exists");
        }
        ServiceCategory c = new ServiceCategory();
        c.setName(req.name().trim());
        c.setDescription(req.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.from(categories.save(c)));
    }

    @PostMapping("/admin/catalog/packages")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<PackageResponse> createPackage(@Valid @RequestBody PackageRequest req) {
        ServiceCategory cat = categories.findById(req.categoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        ServicePackage p = new ServicePackage();
        p.setCategory(cat);
        p.setTitle(req.title().trim());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setDurationMinutes(req.durationMinutes());
        return ResponseEntity.status(HttpStatus.CREATED).body(PackageResponse.from(packages.save(p)));
    }
}