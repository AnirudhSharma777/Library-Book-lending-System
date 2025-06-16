package com.librarysystem.Controller;


import com.librarysystem.Dto.LoginDto;
import com.librarysystem.Dto.RegisterDto;
import com.librarysystem.ResponseDto.LoginResponseDto;
import com.librarysystem.ResponseDto.RegisterResponseDto;
import com.librarysystem.Services.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration and login API")
public class AuthenticationController {

    private final AuthServiceImpl service;


    @Operation(summary = "Register a new user (ADMIN or MEMBER)")
    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto dto) {

        RegisterResponseDto newUser = service.signup(dto);
        return ResponseEntity.ok(newUser);
    }


    @Operation(summary = "Authenticate user and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto dto){
        LoginResponseDto existUser = service.login(dto);
        return ResponseEntity.ok(existUser);
    }

}
