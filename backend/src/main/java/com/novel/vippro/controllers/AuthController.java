package com.novel.vippro.controllers;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.novel.vippro.payload.request.LoginRequest;
import com.novel.vippro.payload.request.SignupRequest;
import com.novel.vippro.payload.response.JwtResponse;
import com.novel.vippro.services.AuthService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
  @Autowired
  AuthService authService;

  @Operation(summary = "Authenticate user", description = "Authenticate a user with email and password and return JWT token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully authenticated", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials"),
      @ApiResponse(responseCode = "400", description = "Invalid input")
  })
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    return authService.authenticateUser(loginRequest);
  }

  @Operation(summary = "Register new user", description = "Register a new user with basic information")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User registered successfully"),
      @ApiResponse(responseCode = "400", description = "Username/Email already exists or invalid input"),
      @ApiResponse(responseCode = "500", description = "Server error during registration")
  })
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    return authService.registerUser(signUpRequest);
  }
}
