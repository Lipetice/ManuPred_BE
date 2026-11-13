package com.fiec.br.back_end.kipper.config;

import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    /** Validade do token em minutos (padrão 30). Pode mudar pela variável JWT_EXPIRACAO_MINUTOS. */
    @Value("${app.jwt.expiracao-minutos:30}")
    private long expiracaoMinutos;

    // A chave é montada uma vez só, em vez de a cada requisição
    private Key chaveAssinatura;

    @PostConstruct
    void inicializar() {
        byte[] bytes = secretKey.getBytes(StandardCharsets.UTF_8);
        // HS256 exige pelo menos 32 bytes. Antes, uma chave curta só dava erro (500) na hora do login.
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET muito curto: precisa ter pelo menos 32 caracteres (tem " + bytes.length + ").");
        }
        this.chaveAssinatura = Keys.hmacShaKeyFor(bytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String generateToken(Users user) {
        return generateToken(null, user);
    }

    public String generateToken(Map<String, Object> claimsExtras, Users user) {
        Map<String, Object> claims = claimsExtras != null ? new HashMap<>(claimsExtras) : new HashMap<>();
        claims.put("id", user.getId().toString());
        claims.put("email", user.getEmail());

        long agora = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(agora))
                .setExpiration(new Date(agora + expiracaoMinutos * 60_000))
                .signWith(chaveAssinatura, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Confere se o token é do usuário informado. A assinatura e a validade já são verificadas
     * ao ler o token (token vencido lança ExpiredJwtException). Antes o token era decodificado
     * três vezes nesta validação; agora é uma só.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject() != null
                && claims.getSubject().equals(userDetails.getUsername())
                && claims.getExpiration() != null
                && claims.getExpiration().after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(chaveAssinatura)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
