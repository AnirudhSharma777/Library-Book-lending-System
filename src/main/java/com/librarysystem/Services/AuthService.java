package com.librarysystem.Services;

import com.librarysystem.Dto.LoginDto;
import com.librarysystem.Dto.RegisterDto;
import com.librarysystem.ResponseDto.LoginResponseDto;
import com.librarysystem.ResponseDto.RegisterResponseDto;

public interface AuthService {

    RegisterResponseDto signup(RegisterDto request);
    LoginResponseDto login(LoginDto request);
}
