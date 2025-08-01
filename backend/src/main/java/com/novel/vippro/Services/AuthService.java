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
    private String normalizeEmail(String email) {
        email = email.trim().toLowerCase();
        String[] parts = email.split("@");
        if (parts.length != 2) return email;
    
        String local = parts[0];
        String domain = parts[1];
    
        if (domain.equals("gmail.com")) {
            local = local.split("\\+")[0];
            local = local.replace(".", "");
        }
    
        return local + "@" + domain;
    }

    public ResponseEntity<ControllerResponse<String>> registerUser(SignupRequest signUpRequest) {
        String username = signUpRequest.getUsername().trim();
        String email = normalizeEmail(signUpRequest.getEmail());
    
        // Username validation: only letters and digits
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            return ResponseEntity
                .badRequest()
                .body(new ControllerResponse<>(false, "Username can only contain letters and numbers!", null, 400));
        }
    
        // Email format validation
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity
                .badRequest()
                .body(new ControllerResponse<>(false, "Invalid email format!", null, 400));
        }
    
        // Check for duplicates
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity
                .badRequest()
                .body(new ControllerResponse<>(false, "Username is already taken!", null, 400));
        }
    
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity
                .badRequest()
                .body(new ControllerResponse<>(false, "Email is already in use!", null, 400));
        }
    
        // Create user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
    
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);
    
        userRepository.save(user);
    
        return ResponseEntity.ok(new ControllerResponse<>(true, "User registered successfully!",
                "User registered successfully!", 200));
    }
}
