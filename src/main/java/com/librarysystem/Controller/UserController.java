package com.librarysystem.Controller;


import com.librarysystem.Entities.User;
import com.librarysystem.Services.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "AdminController", description = "Users API Only Admin Can Access")
public class UserController {

    private final UserServiceImpl service;


    @Operation(summary = "Get user by email", description = "Returns a single user by their email address")
    @GetMapping("/user/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> getUserByEmail(
            @Parameter(description = "Email of the user to retrieve") @PathVariable("email") String email) {
        User user = service.getUserByUsername(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user by ID", description = "Returns a single user by their ID")
    @GetMapping("/user/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> getUserById(
            @Parameter(description = "ID of the user to retrieve") @PathVariable("id") String id) {
        User user = service.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get all users", description = "Returns a list of all users")
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }
}
