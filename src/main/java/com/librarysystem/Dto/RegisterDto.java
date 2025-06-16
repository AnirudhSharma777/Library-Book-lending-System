package com.librarysystem.Dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record RegisterDto(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is Required")
        @Email
        String email,

        @NotBlank(message = "Password is required")
        String password,

//        @Pattern(regexp = "ADMIN|MEMBER", message = "Role must be ADMIN or MEMBER")
        String role
) {
}
