package com.test.news.service.security;

import java.security.Key;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.test.news.model.AuthTokens;
import com.test.news.model.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class JwtTokenProvider {

    private final Key key;
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30min
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30min // 1000 * 60 * 60 * 24 * 7; // 7day

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 사용자 식별값으로 access token 생성
     * @param userId
     * @return AuthTokens
     */
    public AuthTokens generateToken(Long userId, Role role) { 
        // token 생성 시간
        Instant now = Instant.from(OffsetDateTime.now());

        String subject = userId.toString();

        // accessToken 생성
        String accessToken = Jwts.builder()
            .setSubject(subject)
            .claim("roles", role)
            .setExpiration(Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRE_TIME)))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
        
        // refreshToken 생성
        String refreshToken = Jwts.builder()
            .setExpiration(Date.from(now.plusMillis(REFRESH_TOKEN_EXPIRE_TIME)))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();

        return AuthTokens.of(accessToken, refreshToken, BEARER_TYPE, ACCESS_TOKEN_EXPIRE_TIME / 1000L);
    }

    // public String extractSubject(String accessToken) {
    //     Claims claims = parseClaims(accessToken);
    //     return claims.getSubject();
    // }

    /**
     * UsernamePasswordAuthenticationToken으로 인증된 유저인지 확인
     * @param accessToken
     * @return Authentication
     */
    // public Authentication getAuthentication(String accessToken) {
    //     Claims claims = parseClaims(accessToken);

    //     if (claims.get("roles") == null) {
    //         throw new RuntimeException("권한정보가 없는 토큰입니다.");
    //     }

    //     Collection<? extends GrantedAuthority> roles = Arrays.stream(claims.get("roles").toString().split(","))
    //             .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    //     UserDetails user = new User(claims.getSubject(), "", roles);
    //     return new UsernamePasswordAuthenticationToken(user, "", roles);
    // }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
