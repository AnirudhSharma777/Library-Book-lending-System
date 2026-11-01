package com.librarysystem.Services;

import com.librarysystem.Entities.User;
import com.librarysystem.Utils.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLTEyMzQ=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    public void extractUsername_returnsEmailFromGeneratedToken() {
        User user = User.builder().email("harryl423@gmail.com").build();
        String token = jwtService.generateToken(user);
        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(user.getEmail(), extractedUsername);
    }

    @Test
    public void isTokenValid_returnsTrueForAuthenticatedUser() {
        User user = User.builder().email("harryl423@gmail.com").build();
        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token, user);

        assertTrue(isValid);
    }

    @Test
    public void isTokenValid_returnFalseForAnotherUser() throws InterruptedException {
        User user = User.builder().email("harryl423@gmail.com").build();
        User anotherUser = User.builder().email("harryl123@gmail.com").build();
        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token, anotherUser);

        assertFalse(isValid);

    }

    @Test
    public void isTokenValid_throwsExpiredJwtException() throws InterruptedException {
        User user = User.builder().email("harryl423@gmail.com").build();
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);

        String token = jwtService.generateToken(user);

        assertThrows(ExpiredJwtException.class, () ->
            jwtService.isTokenValid(token, user)
        );
    }

    @Test
    public void getExpirationTime_returnExpTime() {
        assertEquals(3600000L, jwtService.getExpirationTime());
    }

    @Test
    public void setShortSecretKey_throwsIllegalArgumentException() {
        User user = User.builder().email("harryl423@gmail.com").build();
        ReflectionTestUtils.setField(jwtService, "secretKey", "c2hvcnQ=");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->jwtService.generateToken(user));
        assertEquals("The secret key must be at least 256 bits (32 bytes) for HS256.", e.getMessage());
    }

    @Test
    public void tamperedJwt_throwsSignatureException() {
        User user = User.builder().email("harryl423@gmail.com").build();
        String token = jwtService.generateToken(user);
        StringBuilder tampered = new StringBuilder(token);
        int i = token.lastIndexOf('.') + 5;
        tampered.setCharAt(i, token.charAt(i) == 'A' ? 'B' : 'A');

        assertThrows(SignatureException.class, () -> jwtService.isTokenValid(tampered.toString(), user));
    }

    @Test
    public void modifiedJwtSecret_throwsSignatureException() {
        User user = User.builder().email("harryl423@gmail.com").build();
        String token = jwtService.generateToken(user);

        ReflectionTestUtils.setField(jwtService, "secretKey", "h6HE/JpugQVrZ48ZSM+R5GYIkzvhXitK6Sztz5deK1Y=");

        assertThrows(SignatureException.class, () -> jwtService.extractUsername(token));
    }

}
