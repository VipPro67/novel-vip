package com.novel.vippro.Services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Auth.LoginRequest;
import com.novel.vippro.DTO.Auth.SignupRequest;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Models.ERole;
import com.novel.vippro.Models.Role;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.JwtResponse;
import com.novel.vippro.Repository.RoleRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Security.JWT.JwtUtils;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RoleApprovalService roleApprovalService;

    public ResponseEntity<ControllerResponse<JwtResponse>> authenticateUser(LoginRequest loginRequest) {
        try {
            logger.info("Attempting to authenticate user: {}", loginRequest.getEmail());
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", loginRequest.getEmail()));
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            JwtResponse jwtResponse = new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles);

            logger.info("User {} authenticated successfully", userDetails.getUsername());
            return ResponseEntity
                    .ok(new ControllerResponse<>(true, "User authenticated successfully", jwtResponse, 200));
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user {}: Invalid credentials", loginRequest.getEmail());
            return ResponseEntity
                    .status(401)
                    .body(new ControllerResponse<>(false, "Invalid username or password", null, 401));
        } catch (ResourceNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity
                    .status(404)
                    .body(new ControllerResponse<>(false, "User not found", null, 404));
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user {}: {}", loginRequest.getEmail(),
                    e.getMessage());
            return ResponseEntity
                    .status(500)
                    .body(new ControllerResponse<>(false, "An unexpected error occurred", null, 500));
        }
    }

    public ResponseEntity<ControllerResponse<String>> registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ControllerResponse<>(false, "Username is already taken!", null, 400));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ControllerResponse<>(false, "Email is already in use!", null, 400));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        // Always assign USER by default
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        // Save the user with USER
        userRepository.save(user);

        // If user requested additional roles (MOD or ADMIN), create approval requests
        if (signUpRequest.getRole() != null && !signUpRequest.getRole().isEmpty()) {
            for (String roleStr : signUpRequest.getRole()) {
                ERole requestedRole = null;

                switch (roleStr.toUpperCase()) {
                    case "ADMIN":
                        requestedRole = ERole.ADMIN;
                        break;
                    case "MOD":
                        requestedRole = ERole.MODERATOR;
                        break;
                    case "USER":
                        // Already assigned, skip
                        continue;
                    default:
                        // Invalid role, skip
                        continue;
                }

                if (requestedRole != null) {
                    roleApprovalService.createRoleApprovalRequest(user, requestedRole);
                }
            }
        }

        return ResponseEntity.ok(new ControllerResponse<>(true, "User registered successfully!",
                "User registered successfully!", 200));
    }
}