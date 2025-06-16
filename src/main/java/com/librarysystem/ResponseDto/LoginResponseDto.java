package com.librarysystem.ResponseDto;

public record LoginResponseDto(
        String token,
        long expiredIn,
        String role

) {
}
