package com.ABIC.CustomerRequest.util;

import com.ABIC.CustomerRequest.web.loginService.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JWTUtil {

    private static final int SECRET_LENGTH = 32;

    public static String generateSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] secretBytes = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(secretBytes);
        return Base64.getEncoder().encodeToString(secretBytes);
    }

    private final String secret=generateSecret();

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);  // Decode the base64-encoded secret
        return Keys.hmacShaKeyFor(keyBytes);  // Recreate the key
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }


    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);  // No try-catch block
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(User user, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();

        // Add user information to the claims
        claims.put("fullname", user.getFullName());
        claims.put("email", user.getEmail());
        claims.put("userNumber", user.getUserNumber());


        // Extract roles from authorities and add them as claims
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority) // Extract the role name
                .collect(Collectors.toList());

        claims.put("roles", roles); // Add the list of roles to the claims

        // Return the generated token with the claims
        return createToken(claims);
    }


    private String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // Set expiration (10 hours)
                .signWith(getSigningKey())  // Sign the token with the secret key
                .compact();  // Generate the token
    }

    public Boolean validateToken(String token) {
        final String extractedUsername = extractEmail(token);
        return  !isTokenExpired(token);
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }

    public Authentication getAuthentication(String username, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    public String extractName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("fullname", String.class);
    }

    public String extractUserNumber(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userNumber", String.class);
    }
}
