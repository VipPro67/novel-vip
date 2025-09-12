package com.novel.vippro.Payload.Response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
  private String accessToken;
  private String type = "Bearer";
  private UUID id;
  private String username;
  private String email;
  private List<String> roles;

  private String refreshToken;
  private Instant accessTokenExpiresAt;
  private Instant refreshTokenExpiresAt;
}
