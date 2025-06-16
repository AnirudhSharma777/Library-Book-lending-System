package com.librarysystem.Utils;

import com.librarysystem.Dto.RegisterDto;
import com.librarysystem.Entities.Role;
import com.librarysystem.Entities.User;
import com.librarysystem.ResponseDto.RegisterResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Mapper {


    private final PasswordEncoder passwordEncoder;

    public User maptoUser(RegisterDto request){
        return User.builder()
                        .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role().toUpperCase()))
                .build();

    }

    public RegisterResponseDto maptoRegisterReponseDto(User savedUser) {

        return new RegisterResponseDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().toString()
        );
    }
}
