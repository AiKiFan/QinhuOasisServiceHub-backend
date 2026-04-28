package com.qinhu.oasis.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 生成与解析工具（基于 jjwt 0.12.5，HS256 算法）
 * <p>Token 载荷：sub=userId、role=用户角色、iat=签发时间、exp=过期时间</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成 JWT Token
     *
     * @param userId 用户 ID
     * @param role   用户角色（参见 {@link com.qinhu.oasis.common.constant.UserRole}）
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(Long userId, Integer role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration * 1000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 JWT Token，返回载荷 Claims
     *
     * @param token JWT 字符串
     * @return 解析后的载荷
     * @throws io.jsonwebtoken.JwtException Token 无效或已过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
