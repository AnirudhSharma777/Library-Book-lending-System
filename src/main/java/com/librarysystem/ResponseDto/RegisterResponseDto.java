package com.librarysystem.ResponseDto;

public record RegisterResponseDto(
        String id,
        String name,
        String email,
//        String password,
        String role
) {
}
