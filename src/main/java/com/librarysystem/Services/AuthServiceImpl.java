package com.librarysystem.Services;

import com.librarysystem.Dto.LoginDto;
import com.librarysystem.Dto.RegisterDto;
import com.librarysystem.Entities.User;
import com.librarysystem.Exception.AccountAlreadyExistsException;
import com.librarysystem.Exception.ResourceNotFoundException;
import com.librarysystem.Exception.SignupFailedException;
import com.librarysystem.Exception.UnauthorizedException;
import com.librarysystem.Repositories.UserRepository;
import com.librarysystem.ResponseDto.LoginResponseDto;
import com.librarysystem.ResponseDto.RegisterResponseDto;
import com.librarysystem.Utils.JwtService;
import com.librarysystem.Utils.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final Mapper mapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public RegisterResponseDto signup(RegisterDto request) {

        try {

            boolean isEmailExist = userRepository.findByEmail(request.email()).isPresent();
            if(isEmailExist){
                throw new AccountAlreadyExistsException("Account already exists using this Email");
            }

            User user = mapper.maptoUser(request);

            User savedUser = userRepository.save(user);

            return mapper.maptoRegisterReponseDto(savedUser);

        } catch (Exception e) {
            throw new SignupFailedException("Registration failed. Please try again later.");
        }
    }

    @Override
    public LoginResponseDto login(LoginDto request) {
        try {

            var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not Found"));

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            // If authentication is successful, generate JWT
            User userDetails = (User) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);
            long expiredIn = jwtService.getExpirationTime();

            return new LoginResponseDto(jwt,expiredIn , userDetails.getRole().name());

        } catch (Exception e) {
            throw new UnauthorizedException("Invalid username or password: " + e.getMessage());
        }
    }
}
