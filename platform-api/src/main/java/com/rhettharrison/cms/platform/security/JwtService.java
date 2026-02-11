package com.rhettharrison.cms.platform.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final Algorithm algorithm;
  private final JWTVerifier verifier;
  private final String issuer;
  private final long expirationSeconds;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.expiration-seconds}") long expirationSeconds
  ) {
    this.algorithm = Algorithm.HMAC256(secret);
    this.issuer = issuer;
    this.expirationSeconds = expirationSeconds;
    this.verifier = JWT.require(algorithm).withIssuer(issuer).build();
  }

  public String issueToken(UUID tenantId, String username, List<String> roles) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expirationSeconds);
    return JWT.create()
        .withIssuer(issuer)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(exp))
        .withSubject(username)
        .withClaim("tenant_id", tenantId.toString())
        .withArrayClaim("roles", roles.toArray(String[]::new))
        .sign(algorithm);
  }

  public DecodedJWT verify(String token) {
    return verifier.verify(token);
  }
}
