package com.novel.vippro.services;

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

import com.novel.vippro.models.ERole;
import com.novel.vippro.models.Role;
import com.novel.vippro.models.User;
import com.novel.vippro.payload.request.LoginRequest;
import com.novel.vippro.payload.request.SignupRequest;
import com.novel.vippro.payload.response.JwtResponse;
import com.novel.vippro.payload.response.MessageResponse;
import com.novel.vippro.repository.RoleRepository;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.security.jwt.JwtUtils;
import com.novel.vippro.security.services.UserDetailsImpl;

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

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        try {
            logger.info("Attempting to authenticate user: {}", loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            logger.info("User {} authenticated successfully", userDetails.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles));
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user {}: Invalid credentials", loginRequest.getUsername());
            return ResponseEntity
                    .status(401)
                    .body(new MessageResponse("Error: Invalid username or password"));
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user {}: {}", loginRequest.getUsername(),
                    e.getMessage());
            return ResponseEntity
                    .status(500)
                    .body(new MessageResponse("Error: An unexpected error occurred"));
        }
    }

    public ResponseEntity<?> registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        // Always assign ROLE_USER by default
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        // Save the user with ROLE_USER
        userRepository.save(user);

        // If user requested additional roles (MOD or ADMIN), create approval requests
        if (signUpRequest.getRole() != null && !signUpRequest.getRole().isEmpty()) {
            for (String roleStr : signUpRequest.getRole()) {
                ERole requestedRole = null;

                switch (roleStr.toUpperCase()) {
                    case "ADMIN":
                    case "ROLE_ADMIN":
                        requestedRole = ERole.ROLE_ADMIN;
                        break;
                    case "MOD":
                    case "MODERATOR":
                    case "ROLE_MODERATOR":
                        requestedRole = ERole.ROLE_MODERATOR;
                        break;
                    case "USER":
                    case "ROLE_USER":
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

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}