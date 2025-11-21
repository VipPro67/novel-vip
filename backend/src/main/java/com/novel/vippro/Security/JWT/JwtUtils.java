package com.novel.vippro.Security.JWT;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.novel.vippro.Security.UserDetailsImpl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${JWT_SECRET:VGhpcyBpcyBhIHNlY3JldCBrZXkgZm9yIEpXVCB0b2tlbiBnZW5lZXJhdGlvbg==}")
  private String jwtSecret;

  @Value("${JWT_EXPIRATION:864000}")
  private int jwtExpirationMs;

  @Value("${JWT_REFRESH_EXPIRATION:25920000}")
  private int jwtRefreshExpirationMs;

  // ====== ACCESS TOKEN (existing) ======
  public String generateJwtToken(Authentication authentication) {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userPrincipal.getId());
    claims.put("email", userPrincipal.getEmail());
    claims.put("roles", userPrincipal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList()));
    claims.put("typ", "access");

    return Jwts.builder()
        .setSubject(userPrincipal.getUsername())
        .addClaims(claims)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  /** Generate an access token when you only have the username (e.g. refresh flow). */
  public String generateAccessTokenFromUsername(String username) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("typ", "access");
    return Jwts.builder()
        .setSubject(username)
        .addClaims(claims)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  // ====== REFRESH TOKEN (new) ======
  /** Generate a refresh token; subject is username. */
  public String generateRefreshToken(String username) {
    // keep claims minimal; include a 'typ' guard
    Map<String, Object> claims = new HashMap<>();
    claims.put("typ", "refresh");
    return Jwts.builder()
        .setSubject(username)
        .addClaims(claims)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  /** Validate structure/signature/expiry AND ensure it's a refresh token. */
  public boolean isRefreshToken(String token) {
    try {
      Claims c = Jwts.parserBuilder().setSigningKey(key()).build()
          .parseClaimsJws(token).getBody();
      Object typ = c.get("typ");
      return "refresh".equals(typ);
    } catch (JwtException | IllegalArgumentException e) {
      logger.error("Refresh token invalid: {}", e.getMessage());
      return false;
    }
  }

  // ====== Helpers ======
  public String getUserNameFromJwtToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key()).build()
        .parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
      return true;
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    } catch (Exception e) {
      logger.error("JWT token validation failed: {}", e.getMessage());
    }
    return false;
  }

  public Date getAccessTokenExpiryDate() {
    return new Date(System.currentTimeMillis() + jwtExpirationMs);
  }

  public Date getRefreshTokenExpiryDate() {
    return new Date(System.currentTimeMillis() + jwtRefreshExpirationMs);
  }

  public long getRefreshTokenExpirationMs() {
    return jwtRefreshExpirationMs;
  }

  public Key getSigningKey() {
    return key();
  }

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }
}
