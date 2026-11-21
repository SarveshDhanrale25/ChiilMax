package com.urbanservice.app.config;

import com.urbanservice.app.catalog.*;
import com.urbanservice.app.users.User;
import com.urbanservice.app.users.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository users;
    private final ServiceCategoryRepository categories;
    private final ServicePackageRepository packages;
    private final PasswordEncoder encoder;

    @Value("${app.bootstrap-admin.email}")
    private String adminEmail;

    @Value("${app.bootstrap-admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository users, ServiceCategoryRepository categories,
                           ServicePackageRepository packages, PasswordEncoder encoder) {
        this.users = users;
        this.categories = categories;
        this.packages = packages;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmail(adminEmail.toLowerCase())) {
            User admin = new User();
            admin.setFullName("Platform Admin");
            admin.setEmail(adminEmail.toLowerCase());
            admin.setPhone("9999999999");
            admin.setPasswordHash(encoder.encode(adminPassword));
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.Status.ACTIVE);
            users.save(admin);
        }

        if (categories.count() == 0) {
            ServiceCategory ac = new ServiceCategory();
            ac.setName("AC & Appliance Repair");
            ac.setDescription("Servicing, gas refill, and installation for split and window ACs");
            ac = categories.save(ac);

            ServiceCategory cleaning = new ServiceCategory();
            cleaning.setName("Home Cleaning");
            cleaning.setDescription("Deep cleaning for bathrooms, kitchens, and full homes");
            cleaning = categories.save(cleaning);

            ServicePackage p1 = new ServicePackage();
            p1.setCategory(ac);
            p1.setTitle("Foam-Jet AC Service");
            p1.setDescription("Deep coil wash, drain tray cleaning, and cooling check");
            p1.setPrice(new BigDecimal("599.00"));
            p1.setDurationMinutes(60);
            packages.save(p1);

            ServicePackage p2 = new ServicePackage();
            p2.setCategory(cleaning);
            p2.setTitle("Intense Bathroom Cleaning (2 Bathrooms)");
            p2.setDescription("Hard-water stain removal, tile scrubbing, and sanitization");
            p2.setPrice(new BigDecimal("899.00"));
            p2.setDurationMinutes(90);
            packages.save(p2);
        }
    }
}