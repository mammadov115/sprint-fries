package com.sprintfries.api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:SprintFriesSuperSecretKeyForJWTTokenGeneration2026}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long expiration;

    /**
     * Generates a secure JWT for a user.
     * Includes email as subject, and role & tenant as custom claims.
     */
    public String generateToken(String email, String role, String tenant) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withClaim("tenant", tenant)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(algorithm);
    }

    /**
     * Verifies the given token and returns the decoded JWT if valid.
     * Throws an exception if the token is invalid or expired.
     */
    public DecodedJWT verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }
}
