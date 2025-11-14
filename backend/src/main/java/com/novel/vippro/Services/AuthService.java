package com.novel.vippro.Services;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.novel.vippro.DTO.Auth.GoogleAccountInfo;
import com.novel.vippro.DTO.Auth.GoogleAuthRequest;
import com.novel.vippro.DTO.Auth.LoginRequest;
import com.novel.vippro.DTO.Auth.RefreshTokenRequest;
import com.novel.vippro.DTO.Auth.SignupRequest;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Models.ERole;
import com.novel.vippro.Models.Role;
import com.novel.vippro.Models.SystemJob;
import com.novel.vippro.Models.SystemJobStatus;
import com.novel.vippro.Models.SystemJobType;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.JwtResponse;
import com.novel.vippro.Messaging.MessagePublisher;
import com.novel.vippro.Repository.RoleRepository;
import com.novel.vippro.Repository.SystemJobRepository;
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

	@Autowired
	MessagePublisher messagePublisher;

	@Autowired
	SystemJobRepository systemJobRepository;

	@Value("${google.client-id:}")
	private String googleClientId;

	@Value("${auth.email-verification.expiration-hours:24}")
	private long emailVerificationExpirationHours;

	private final NetHttpTransport netHttpTransport = new NetHttpTransport();
	private static final GsonFactory GSON_FACTORY = GsonFactory.getDefaultInstance();

	public ResponseEntity<ControllerResponse<JwtResponse>> authenticateUser(LoginRequest loginRequest) {
		try {
			String normalizedEmail = normalizeEmail(loginRequest.email());
			logger.info("Attempting to authenticate user: {}", normalizedEmail);
			User user = userRepository.findByEmail(normalizedEmail)
					.orElseThrow(() -> new ResourceNotFoundException("User", "email", loginRequest.email()));
			if (Boolean.FALSE.equals(user.getEmailVerified())) {
				sendVerificationEmailSilently(user, false);
				return ResponseEntity.status(403)
						.body(new ControllerResponse<>(false,
								"Please verify your email before signing in. We've sent you a fresh verification link.",
								null, 403));
			}
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(user.getUsername(),
							loginRequest.password()));

			SecurityContextHolder.getContext().setAuthentication(authentication);
			String jwt = jwtUtils.generateJwtToken(authentication);
			logger.info("jwt", jwt);
			String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

			UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
			List<String> roles = userDetails.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority)
					.collect(Collectors.toList());

			JwtResponse jwtResponse = new JwtResponse(jwt, "Bearer",
					userDetails.getId(),
					userDetails.getUsername(),
					userDetails.getEmail(),
					roles, refreshToken, jwtUtils.getAccessTokenExpiryDate().toInstant(),
					jwtUtils.getRefreshTokenExpiryDate().toInstant());

			logger.info("User {} authenticated successfully", userDetails.getUsername());
			return ResponseEntity
					.ok(new ControllerResponse<>(true, "User authenticated successfully",
							jwtResponse, 200));
		} catch (BadCredentialsException e) {
			logger.warn("Authentication failed for user {}: Invalid credentials", loginRequest.email());
			return ResponseEntity
					.status(401)
					.body(new ControllerResponse<>(false, "Invalid username or password", null,
							401));
		} catch (ResourceNotFoundException e) {
			logger.error("User not found: {}", e.getMessage());
			return ResponseEntity
					.status(404)
					.body(new ControllerResponse<>(false, "User not found", null, 404));
		} catch (Exception e) {
			logger.error("Unexpected error during authentication for user {}: {}", loginRequest.email(),
					e.getMessage());
			return ResponseEntity
					.status(500)
					.body(new ControllerResponse<>(false, "An unexpected error occurred", null,
							500));
		}
	}

	public ResponseEntity<ControllerResponse<JwtResponse>> refreshAccessToken(RefreshTokenRequest req) {
		String requestToken = req.refreshToken();
		if (requestToken == null || requestToken.isEmpty()) {
			return ResponseEntity
					.badRequest()
					.body(new ControllerResponse<>(false, "Refresh token is required", null, 400));
		}

		if (!jwtUtils.validateJwtToken(requestToken) || !jwtUtils.isRefreshToken(requestToken)) {
			return ResponseEntity
					.status(403)
					.body(new ControllerResponse<>(false, "Invalid or expired refresh token", null,
							403));
		}

		String username = jwtUtils.getUserNameFromJwtToken(requestToken);
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

		String newAccessToken = jwtUtils.generateAccessTokenFromUsername(username);
		String newRefreshToken = jwtUtils.generateRefreshToken(username);

		UserDetailsImpl userDetails = UserDetailsImpl.build(user);
		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		JwtResponse jwtResponse = new JwtResponse(newAccessToken, "Bearer",
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				roles, newRefreshToken, jwtUtils.getAccessTokenExpiryDate().toInstant(),
				jwtUtils.getRefreshTokenExpiryDate().toInstant());

		return ResponseEntity
				.ok(new ControllerResponse<>(true, "Access token refreshed successfully",
						jwtResponse, 200));
	}

	private String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		email = email.trim().toLowerCase();
		String[] parts = email.split("@");
		if (parts.length != 2)
			return email;

		String local = parts[0];
		String domain = parts[1];

		if (domain.equals("gmail.com")) {
			local = local.split("\\+")[0];
			local = local.replace(".", "");
		}

		return local + "@" + domain;
	}

	public ResponseEntity<ControllerResponse<String>> registerUser(SignupRequest signUpRequest) {
		String username = signUpRequest.username().trim();
		String email = normalizeEmail(signUpRequest.email());

		// Username validation: only letters and digits
		if (!username.matches("^[a-zA-Z0-9]+$")) {
			return ResponseEntity
					.badRequest()
					.body(new ControllerResponse<>(false,
							"Username can only contain letters and numbers!", null, 400));
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
		user.setPassword(encoder.encode(signUpRequest.password()));

		List<Role> roles = new ArrayList<>();
		Role userRole = roleRepository.findByName(ERole.USER)
				.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		roles.add(userRole);
		user.setRoles(roles);
		prepareVerificationToken(user);

		User savedUser = userRepository.save(user);
		Duration validity = getVerificationDuration();
		try {
			queueEmailVerification(savedUser, validity);
		} catch (RuntimeException ex) {
			logger.error("Failed to enqueue verification email for {}", savedUser.getEmail(), ex);
			userRepository.delete(savedUser);
			return ResponseEntity.status(500)
					.body(new ControllerResponse<>(false,
							"We couldn't queue the verification email. Please try again in a moment.",
							null, 500));
		}

		return ResponseEntity.ok(new ControllerResponse<>(true,
				"Account created. Check your inbox to verify your email before signing in.",
				"Verification email sent", 200));
	}

	public ResponseEntity<ControllerResponse<String>> verifyEmail(String token) {
		if (token == null || token.isBlank()) {
			return ResponseEntity.badRequest()
					.body(new ControllerResponse<>(false, "Verification token is required", null, 400));
		}

		User user = userRepository.findByEmailVerificationToken(token).orElse(null);
		if (user == null) {
			return ResponseEntity.status(400)
					.body(new ControllerResponse<>(false, "Invalid or unknown verification token.", null, 400));
		}

		if (Boolean.TRUE.equals(user.getEmailVerified())) {
			return ResponseEntity.ok(new ControllerResponse<>(true,
					"Email is already verified. You can sign in now.",
					"Email already verified", 200));
		}

		if (isVerificationTokenExpired(user)) {
			sendVerificationEmailSilently(user, true);
			return ResponseEntity.status(400)
					.body(new ControllerResponse<>(false,
							"This verification link has expired. We've sent a new link to your email.",
							null, 400));
		}

		user.setEmailVerified(Boolean.TRUE);
		user.setEmailVerifiedAt(Instant.now());
		user.setEmailVerificationToken(null);
		user.setEmailVerificationExpiresAt(null);
		user.setEmailVerificationSentAt(null);
		userRepository.save(user);

		return ResponseEntity.ok(new ControllerResponse<>(true,
				"Email verified successfully. You can now sign in.",
				"Email verified", 200));
	}

	public ResponseEntity<ControllerResponse<GoogleAccountInfo>> verifyGoogleAccount(GoogleAuthRequest request) {
		try {
			Payload payload = verifyGoogleIdToken(request.credential());
			String email = payload.getEmail();
			String normalizedEmail = email != null ? normalizeEmail(email) : null;
			GoogleAccountInfo info = GoogleAccountInfo.builder()
					.email(normalizedEmail)
					.emailVerified(Boolean.TRUE.equals(payload.getEmailVerified()))
					.fullName(valueOrNull(payload.get("name")))
					.avatar(valueOrNull(payload.get("picture")))
					.subject(payload.getSubject())
					.build();
			return ResponseEntity.ok(new ControllerResponse<>(true, "Google account verified", info, 200));
		} catch (BadCredentialsException | IllegalArgumentException e) {
			return ResponseEntity.status(400)
					.body(new ControllerResponse<>(false, e.getMessage(), null, 400));
		} catch (IllegalStateException e) {
			logger.error("Google client configuration error: {}", e.getMessage());
			return ResponseEntity.status(500)
					.body(new ControllerResponse<>(false, "Google authentication is not configured", null, 500));
		} catch (GeneralSecurityException | IOException e) {
			logger.error("Failed to verify Google credential", e);
			return ResponseEntity.status(502)
					.body(new ControllerResponse<>(false, "Unable to verify Google credential", null, 502));
		} catch (Exception e) {
			logger.error("Unexpected error verifying Google account", e);
			return ResponseEntity.status(500)
					.body(new ControllerResponse<>(false, "Unexpected error verifying Google account", null, 500));
		}
	}

	public ResponseEntity<ControllerResponse<JwtResponse>> loginOrRegisterWithGoogle(GoogleAuthRequest request) {
		try {
			Payload payload = verifyGoogleIdToken(request.credential());
			if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
				return ResponseEntity.status(400)
						.body(new ControllerResponse<>(false, "Google email is not verified", null, 400));
			}

			String email = payload.getEmail();
			if (email == null || email.isBlank()) {
				throw new IllegalArgumentException("Google account does not include an email address");
			}
			String normalizedEmail = normalizeEmail(email);

			User user = userRepository.findByEmail(normalizedEmail)
					.map(existing -> updateExistingUserFromGoogle(existing, payload))
					.orElseGet(() -> createUserFromGooglePayload(payload, normalizedEmail));

			JwtResponse jwtResponse = buildJwtResponse(user);
			return ResponseEntity.ok(new ControllerResponse<>(true, "Authenticated with Google", jwtResponse, 200));
		} catch (BadCredentialsException | IllegalArgumentException e) {
			logger.warn("Google authentication failed: {}", e.getMessage());
			return ResponseEntity.status(400)
					.body(new ControllerResponse<>(false, e.getMessage(), null, 400));
		} catch (IllegalStateException e) {
			logger.error("Google client configuration error: {}", e.getMessage());
			return ResponseEntity.status(500)
					.body(new ControllerResponse<>(false, "Google authentication is not configured", null, 500));
		} catch (GeneralSecurityException | IOException e) {
			logger.error("Failed to verify Google credential", e);
			return ResponseEntity.status(502)
					.body(new ControllerResponse<>(false, "Unable to verify Google credential", null, 502));
		} catch (Exception e) {
			logger.error("Unexpected error while processing Google login", e);
			return ResponseEntity.status(500)
					.body(new ControllerResponse<>(false, "Unexpected error during Google authentication", null, 500));
		}
	}

	private Payload verifyGoogleIdToken(String credential) throws GeneralSecurityException, IOException {
		if (credential == null || credential.isBlank()) {
			throw new IllegalArgumentException("Google credential is required");
		}
		if (googleClientId == null || googleClientId.isBlank()) {
			throw new IllegalStateException("Google client ID is not configured");
		}

		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(netHttpTransport, GSON_FACTORY)
				.setAudience(Collections.singletonList(googleClientId))
				.build();

		GoogleIdToken idToken = verifier.verify(credential);
		if (idToken == null) {
			throw new BadCredentialsException("Invalid Google ID token");
		}
		return idToken.getPayload();
	}

	private User createUserFromGooglePayload(Payload payload, String normalizedEmail) {
		User user = new User();
		user.setEmail(normalizedEmail);
		user.setUsername(generateUniqueUsername(valueOrNull(payload.get("given_name")), normalizedEmail));
		user.setFullName(valueOrNull(payload.get("name")));
		user.setAvatar(valueOrNull(payload.get("picture")));
		user.setPassword(encoder.encode(UUID.randomUUID().toString()));
		user.setEmailVerified(Boolean.TRUE);
		user.setEmailVerifiedAt(Instant.now());
		user.setEmailVerificationToken(null);
		user.setEmailVerificationExpiresAt(null);
		user.setEmailVerificationSentAt(null);

		Role userRole = roleRepository.findByName(ERole.USER)
				.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		user.setRoles(Collections.singletonList(userRole));

		return userRepository.save(user);
	}

	private User updateExistingUserFromGoogle(User user, Payload payload) {
		boolean updated = false;
		String fullName = valueOrNull(payload.get("name"));
		if (fullName != null && (user.getFullName() == null || user.getFullName().isBlank())) {
			user.setFullName(fullName);
			updated = true;
		}

		String avatar = valueOrNull(payload.get("picture"));
		if (avatar != null && (user.getAvatar() == null || user.getAvatar().isBlank())) {
			user.setAvatar(avatar);
			updated = true;
		}

		if (!Boolean.TRUE.equals(user.getEmailVerified())) {
			user.setEmailVerified(Boolean.TRUE);
			user.setEmailVerifiedAt(Instant.now());
			user.setEmailVerificationToken(null);
			user.setEmailVerificationExpiresAt(null);
			user.setEmailVerificationSentAt(null);
			updated = true;
		}

		if (updated) {
			return userRepository.save(user);
		}
		return user;
	}

	private String generateUniqueUsername(String preferredName, String email) {
		String base = preferredName != null && !preferredName.isBlank()
				? preferredName
				: email.split("@")[0];
		base = base.toLowerCase().replaceAll("[^a-z0-9]", "");
		if (base.isBlank()) {
			base = "user";
		}

		String candidate = base;
		int suffix = 0;
		while (userRepository.existsByUsername(candidate)) {
			suffix++;
			candidate = base + suffix;
		}
		return candidate;
	}

	private JwtResponse buildJwtResponse(User user) {
		UserDetailsImpl userDetails = UserDetailsImpl.build(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());
		String accessToken = jwtUtils.generateJwtToken(authentication);
		String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());
		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		return new JwtResponse(accessToken,
				"Bearer",
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				roles,
				refreshToken,
				jwtUtils.getAccessTokenExpiryDate().toInstant(),
				jwtUtils.getRefreshTokenExpiryDate().toInstant());
	}

	private Duration getVerificationDuration() {
		long hours = emailVerificationExpirationHours > 0 ? emailVerificationExpirationHours : 24L;
		return Duration.ofHours(hours);
	}

	private void prepareVerificationToken(User user) {
		Instant now = Instant.now();
		user.setEmailVerificationToken(UUID.randomUUID().toString());
		user.setEmailVerificationSentAt(now);
		user.setEmailVerificationExpiresAt(now.plus(getVerificationDuration()));
		user.setEmailVerified(Boolean.FALSE);
		user.setEmailVerifiedAt(null);
	}

	private boolean isVerificationTokenExpired(User user) {
		Instant expiresAt = user.getEmailVerificationExpiresAt();
		return expiresAt != null && Instant.now().isAfter(expiresAt);
	}

	private void queueEmailVerification(User user, Duration validity) {
		if (user == null || user.getId() == null) {
			throw new IllegalStateException("User must be persisted before queuing verification email");
		}
		Duration effectiveValidity = validity != null ? validity : getVerificationDuration();
		SystemJob job = new SystemJob();
		job.setJobType(SystemJobType.EMAIL_VERIFICATION);
		job.setStatus(SystemJobStatus.QUEUED);
		job.setUserId(user.getId());
		job.setStatusMessage("Queued email verification");
		systemJobRepository.save(job);
		messagePublisher.publishEmailVerification(user.getId(), effectiveValidity);
	}

	private void sendVerificationEmailSilently(User user, boolean forceNewToken) {
		try {
			Duration validity = getVerificationDuration();
			if (forceNewToken || user.getEmailVerificationToken() == null || isVerificationTokenExpired(user)) {
				prepareVerificationToken(user);
			} else {
				Instant now = Instant.now();
				user.setEmailVerificationSentAt(now);
				user.setEmailVerificationExpiresAt(now.plus(validity));
			}
			userRepository.save(user);
			queueEmailVerification(user, validity);
		} catch (Exception e) {
			logger.error("Unable to queue verification email for {}", user.getEmail(), e);
		}
	}

	private String valueOrNull(Object value) {
		return value == null ? null : value.toString();
	}
}
