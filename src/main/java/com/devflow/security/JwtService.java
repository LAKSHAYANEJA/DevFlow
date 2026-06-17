package com.devflow.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    // ----- GENERATE TOKENS -----

    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder().
        subject(userDetails.getUsername()).
        issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
        .signWith(getSigningKey())
        .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder().
        subject(userDetails.getUsername()).
        claim("type", "refresh").
        issuedAt(new Date()).
        expiration(new Date(System.currentTimeMillis() + refreshTokenExpiryMs)).
        signWith(getSigningKey()).
        compact();
    }

    // ----- VALIDATE TOKEN -----

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    // ----- EXTRACT CLAIMS -----

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().
        verifyWith(getSigningKey()).
        build().
        parseSignedClaims(token).
        getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
