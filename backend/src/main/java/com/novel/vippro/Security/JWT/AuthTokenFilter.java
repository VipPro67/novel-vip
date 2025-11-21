package com.novel.vippro.Security.JWT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.novel.vippro.Security.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class AuthTokenFilter extends OncePerRequestFilter {
  @Autowired
  private JwtUtils jwtUtils;

  private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = parseJwt(request);
      if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
        try {
          Claims claims = Jwts.parserBuilder()
              .setSigningKey(jwtUtils.getSigningKey())
              .build()
              .parseClaimsJws(jwt)
              .getBody();

          String username = claims.getSubject();
          String email = claims.get("email", String.class);
          String userId = claims.get("userId", String.class);

          @SuppressWarnings("unchecked")
          List<String> roles = (List<String>) claims.get("roles");
          @SuppressWarnings("unchecked")
          List<String> permissions = (List<String>) claims.get("permissions");
          List<SimpleGrantedAuthority> authorities = new ArrayList<>();
          if (roles != null) {
            for (String role : roles) {
              authorities.add(new SimpleGrantedAuthority(role));
            }
          }
          if (permissions != null) {
            for (String permission : permissions) {
              authorities.add(new SimpleGrantedAuthority(permission));
            }
          }

          UserDetailsImpl userDetails = new UserDetailsImpl(
              UUID.fromString(userId),
              username,
              email,
              null,
              authorities);

          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              authorities);

          // Ensure request is not null
          if (request == null) {
            logger.error("HttpServletRequest is null");
            filterChain.doFilter(request, response);
            return;
          }

          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // Ensure SecurityContext is initialized
          if (SecurityContextHolder.getContext() == null) {
            logger.error("SecurityContext is null");
            filterChain.doFilter(request, response);
            return;
          }
          try {
            SecurityContextHolder.getContext().setAuthentication(authentication);
          } catch (Exception e) {
          }
        } catch (IllegalArgumentException e) {
          logger.error("Invalid UUID or argument: {}", e.getMessage());
        } catch (ClassCastException e) {
          logger.error("Type mismatch in JWT claims: {}", e.getMessage());
        } catch (io.jsonwebtoken.io.DecodingException e) {
          logger.error("JWT token contains invalid Base64 characters: {}", e.getMessage());
        } catch (Exception e) {
          logger.error("Error while parsing JWT claims: {}", e.getMessage(), e);
        }
      }
    } catch (Exception e) {
      logger.error("Cannot process authentication: {}", e.getMessage(), e);
    }

    filterChain.doFilter(request, response);
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;
  }
}
