package com.maksim.project.service;

import com.maksim.project.context.ApplicationContextProvider;
import com.maksim.project.model.Permission;
import com.maksim.project.model.Role;
import com.maksim.project.model.User;
import com.maksim.project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private PermissionRepository permissionRepository;

    private RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository,RoleRepository roleRepository,PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User myUser = this.userRepository.findByEmail(email);
        if(myUser == null)
            throw new UsernameNotFoundException("User with email: " + email + " not found.");

        Set<GrantedAuthority> authorities = myUser.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toSet());

        // Dodavanje role korisnika (pretpostavljamo da `myUser.getRole()` vraća objekat Role)
        authorities.add(new SimpleGrantedAuthority(myUser.getRole().getName()));

        return new org.springframework.security.core.userdetails.User(myUser.getEmail(), myUser.getPassword(), authorities);
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

//    public User createUser(User user) {
//        // Validate permissions
//        Set<String> allowedPermissions = Set.of("can_create_users", "can_read_users", "can_update_users","can_delete_users","can_place_order","can_schedule_order","can_search_order","can_track_order");
//        if (!user.getPermissions().stream().allMatch(p -> allowedPermissions.contains(p.getName()))) {
//            throw new IllegalArgumentException("Invalid permissions. Only 'can_create_users', 'can_read_users', 'can_update_users', and 'can_delete_users' are allowed.");
//        }
//
//        // Fetch existing permissions
//        Set<Permission> permissions = new HashSet<>();
//        for (String permissionName : user.getPermissions().stream().map(Permission::getName).toList()) {
//            permissions.add(permissionRepository.findByName(permissionName)
//                    .orElseThrow(() -> new DataIntegrityViolationException("Permission '" + permissionName + "' not found.")));
//        }
//
//        // Hash the password
//        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
//        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
//        String hashedPassword = passwordEncoder.encode(user.getPassword());
//        user.setPassword(hashedPassword);
//
//        user.setPermissions(permissions);
//
//        return userRepository.save(user);
//    }

    public User createUser(User user) {
        // Validacija permisija
        Set<String> allowedPermissions = Set.of("can_create_users", "can_read_users", "can_update_users", "can_delete_users",
                "can_place_order", "can_schedule_order", "can_search_order", "can_track_order");
        if (!user.getPermissions().stream().allMatch(p -> allowedPermissions.contains(p.getName()))) {
            throw new IllegalArgumentException("Invalid permissions. Only allowed permissions can be assigned.");
        }

        // Fetchovanje permisija
        Set<Permission> permissions = new HashSet<>();
        for (String permissionName : user.getPermissions().stream().map(Permission::getName).toList()) {
            permissions.add(permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new DataIntegrityViolationException("Permission '" + permissionName + "' not found.")));
        }

        // Proveri i postavi rolu
        Role role = roleRepository.findById(user.getRole().getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + user.getRole().getRoleId()));
        user.setRole(role);

        // Hashovanje lozinke
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        user.setPermissions(permissions);

        // Sačuvaj korisnika
        return userRepository.save(user);
    }




    public User updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate permissions (if applicable)
        if (updatedUser.getPermissions() != null) {
            Set<String> allowedPermissions = Set.of("can_create_users", "can_read_users", "can_update_users", "can_delete_users");
            if (!updatedUser.getPermissions().stream().allMatch(p -> allowedPermissions.contains(p.getName()))) {
                throw new IllegalArgumentException("Invalid permissions. Only 'can_create_users', 'can_read_users', 'can_update_users', and 'can_delete_users' are allowed.");
            }

            Set<Permission> permissions = new HashSet<>();
            for (String permissionName : updatedUser.getPermissions().stream().map(Permission::getName).toList()) {
                permissions.add(permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new DataIntegrityViolationException("Permission '" + permissionName + "' not found.")));
            }
            existingUser.setPermissions(permissions);
        }

        // Update user data
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());

        // If password is provided, hash and update
//        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
//            ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
//            PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
//            String hashedPassword = passwordEncoder.encode(updatedUser.getPassword());
//            existingUser.setPassword(hashedPassword);
//        }

        return userRepository.save(existingUser);
    }

    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}

