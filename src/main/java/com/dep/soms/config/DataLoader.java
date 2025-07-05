//package com.dep.soms.config;
//
//import com.dep.soms.model.Role;
//import com.dep.soms.model.User;
//import com.dep.soms.repository.RoleRepository;
//import com.dep.soms.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//
//@Component
//public class DataLoader implements CommandLineRunner {
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("Initializing data...");
//
//        // Create roles if they don't exist
//        createRoleIfNotFound(Role.ERole.ROLE_ADMIN);
//        createRoleIfNotFound(Role.ERole.ROLE_MANAGER);
//        createRoleIfNotFound(Role.ERole.ROLE_GUARD);
//        createRoleIfNotFound(Role.ERole.ROLE_CLIENT);
//
//        System.out.println("Roles created successfully");
//
//        // Create admin user if it doesn't exist
//        if (!userRepository.existsByUsername("admin")) {
//            User admin = new User();
//            admin.setUsername("admin");
//            admin.setEmail("admin@soms.com");
//            admin.setPassword(passwordEncoder.encode("admin123"));
//            admin.setFirstName("Admin");
//            admin.setLastName("User");
//            admin.setActive(true);
//            admin.setCreatedAt(LocalDateTime.now());
//
//            Set<Role> roles = new HashSet<>();
//            Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
//                    .orElseThrow(() -> new RuntimeException("Error: Admin Role is not found."));
//            roles.add(adminRole);
//            admin.setRoles(roles);
//
//            User savedAdmin = userRepository.save(admin);
//            System.out.println("Admin user created successfully with ID: " + savedAdmin.getId());
//        } else {
//            System.out.println("Admin user already exists");
//        }
//    }
//
//    private void createRoleIfNotFound(Role.ERole name) {
//        if (!roleRepository.findByName(name).isPresent()) {
//            Role role = new Role();
//            role.setName(name);
//            roleRepository.save(role);
//            System.out.println("Created role: " + name);
//        } else {
//            System.out.println("Role already exists: " + name);
//        }
//    }
//}
//

package com.dep.soms.config;

import com.dep.soms.model.Role;
import com.dep.soms.model.User;
import com.dep.soms.repository.RoleRepository;
import com.dep.soms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing data...");

        // Create roles if they don't exist
        createRoleIfNotFound(Role.ERole.ROLE_ADMIN);
        createRoleIfNotFound(Role.ERole.ROLE_MANAGER);
        createRoleIfNotFound(Role.ERole.ROLE_GUARD);
        createRoleIfNotFound(Role.ERole.ROLE_CLIENT);
        createRoleIfNotFound(Role.ERole.ROLE_HR);
        createRoleIfNotFound(Role.ERole.ROLE_SUPERVISOR);

        System.out.println("Roles created successfully");

        // Create admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@soms.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now());

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Admin Role is not found."));
            roles.add(adminRole);
            admin.setRoles(roles);

            User savedAdmin = userRepository.save(admin);
            System.out.println("Admin user created successfully with ID: " + savedAdmin.getId());
        } else {
            System.out.println("Admin user already exists");
        }

        // Create HR user if it doesn't exist
        if (!userRepository.existsByUsername("hr")) {
            User hr = new User();
            hr.setUsername("hr");
            hr.setEmail("hr@soms.com");
            hr.setPassword(passwordEncoder.encode("hr123"));
            hr.setFirstName("HR");
            hr.setLastName("Manager");
            hr.setActive(true);
            hr.setCreatedAt(LocalDateTime.now());

            Set<Role> roles = new HashSet<>();
            Role hrRole = roleRepository.findByName(Role.ERole.ROLE_HR)
                    .orElseThrow(() -> new RuntimeException("Error: HR Role is not found."));
            roles.add(hrRole);
            hr.setRoles(roles);

            User savedHr = userRepository.save(hr);
            System.out.println("HR user created successfully with ID: " + savedHr.getId());
        } else {
            System.out.println("HR user already exists");
        }

        // Create Supervisor user if it doesn't exist
        if (!userRepository.existsByUsername("supervisor")) {
            User supervisor = new User();
            supervisor.setUsername("supervisor");
            supervisor.setEmail("supervisor@soms.com");
            supervisor.setPassword(passwordEncoder.encode("supervisor123"));
            supervisor.setFirstName("Supervisor");
            supervisor.setLastName("User");
            supervisor.setActive(true);
            supervisor.setCreatedAt(LocalDateTime.now());

            Set<Role> roles = new HashSet<>();
            Role hrRole = roleRepository.findByName(Role.ERole.ROLE_SUPERVISOR)
                    .orElseThrow(() -> new RuntimeException("Error: Supervisor Role is not found."));
            roles.add(hrRole);
            supervisor.setRoles(roles);

            User savedHr = userRepository.save(supervisor);
            System.out.println("Supervisor user created successfully with ID: " + savedHr.getId());
        } else {
            System.out.println("Supervisor user already exists");
        }

        // Create Manager user if it doesn't exist
        if (!userRepository.existsByUsername("manager")) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setEmail("manager@soms.com");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setFirstName("Manager");
            manager.setLastName("User");
            manager.setActive(true);
            manager.setCreatedAt(LocalDateTime.now());

            Set<Role> roles = new HashSet<>();
            Role managerRole = roleRepository.findByName(Role.ERole.ROLE_MANAGER)
                    .orElseThrow(() -> new RuntimeException("Error: Manager Role is not found."));
            roles.add(managerRole);
            manager.setRoles(roles);

            User savedManager = userRepository.save(manager);
            System.out.println("Manager user created successfully with ID: " + savedManager.getId());
        } else {
            System.out.println("Manager user already exists");
        }

    }



    private void createRoleIfNotFound(Role.ERole name) {
        if (!roleRepository.findByName(name).isPresent()) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
            System.out.println("Created role: " + name);
        } else {
            System.out.println("Role already exists: " + name);
        }
    }
}
