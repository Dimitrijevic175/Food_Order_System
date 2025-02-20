package com.maksim.project.controller;

import com.maksim.project.model.User;
import com.maksim.project.security.CheckSecurity;
import com.maksim.project.service.UserService;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @CheckSecurity(permissions = {"can_read_users"})
    public List<User> getAllUsers(@RequestHeader("Authorization") String authorization) {
        return userService.getAllUsers();
    }


    @PostMapping
    @CheckSecurity(permissions = {"can_create_users"}, message = "You cannot create users")
    public ResponseEntity<User> createUser(@RequestHeader("Authorization") String authorization,@RequestBody User user) {
//        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    @CheckSecurity(permissions = {"can_update_users"})
    public ResponseEntity<User> updateUser(@RequestHeader("Authorization") String authorization,@PathVariable Long userId, @RequestBody User updatedUser) {
        User updated = userService.updateUser(userId, updatedUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @CheckSecurity(permissions = {"can_delete_users"})
    public ResponseEntity<Void> deleteUser(@RequestHeader("Authorization") String authorization,@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @CheckSecurity(permissions = {"can_read_users"})
    public ResponseEntity<User> getUserById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
//    @CheckSecurity(permissions = {"can_read_users"})
    public ResponseEntity<User> getUserByEmail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

}
