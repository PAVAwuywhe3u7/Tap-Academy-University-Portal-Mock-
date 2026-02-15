package com.university.portal.controller;

import com.university.portal.dto.ApiResponse;
import com.university.portal.dto.LoginRequest;
import com.university.portal.dto.LoginResponse;
import com.university.portal.dto.RegisterRequest;
import com.university.portal.dto.UserDTO;
import com.university.portal.entity.User;
import com.university.portal.security.JwtTokenProvider;
import com.university.portal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request.getName(), request.getEmail(), request.getPassword(), request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userService.toDTO(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            String email = request.getEmail().trim().toLowerCase();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(user);

            LoginResponse loginResponse = new LoginResponse(
                    token,
                    "Bearer",
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole().name(),
                    resolveRedirectPath(user)
            );

            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials"));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid token format"));
        }

        String jwt = token.substring(7);
        boolean valid = tokenProvider.validateToken(jwt);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Token is invalid or expired"));
        }

        String username = tokenProvider.getUsernameFromToken(jwt);
        return ResponseEntity.ok(ApiResponse.success("Token is valid", username));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByEmail(username);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", userService.toDTO(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT is stateless, so logout is handled client-side by clearing the token.
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    private String resolveRedirectPath(User user) {
        return switch (user.getRole()) {
            case STUDENT -> "/student-dashboard";
            case FACULTY -> "/faculty-dashboard";
            case ADMIN -> "/admin-dashboard";
        };
    }
}
