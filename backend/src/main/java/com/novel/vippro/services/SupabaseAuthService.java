package com.novel.vippro.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class SupabaseAuthService {

    @Value("${supabase.anon.key}")
    private String supabaseAnonKey;

    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(supabaseAnonKey)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public boolean isAuthenticated(String token) {
        try {
            // Verify the JWT token
            Claims claims = getAllClaimsFromToken(token);

            // Check if the token is for Supabase
            String issuer = claims.getIssuer();
            if (!"supabase".equals(issuer)) {
                return false;
            }

            // Check if the token is not expired
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public void signOut(String token) {
        // In a real implementation, you might want to blacklist the token
        // For now, we'll just verify it's valid
        isAuthenticated(token);
    }
}