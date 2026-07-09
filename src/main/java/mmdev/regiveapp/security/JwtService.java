package mmdev.regiveapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationsMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationsMs) {
        this.key= Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationsMs=expirationsMs;
    }

    public String generateToken(String email,String role){
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("role",role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationsMs)))
                .signWith(key)
                .compact();
    }
    public String extractEmail(String token){
        return parseClaims(token).getSubject();
    }
    public boolean isValid(String token){
        try {
            parseClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }
    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
