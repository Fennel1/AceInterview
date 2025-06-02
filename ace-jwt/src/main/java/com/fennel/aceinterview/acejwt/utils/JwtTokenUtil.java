package com.fennel.aceinterview.acejwt.utils;

import com.fennel.aceinterview.acejwt.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenUtil {
    private static final String JWT_CACHE_KEY = "jwt:userId:";
    private static final String USER_ID = "userId";
    private static final String USER_NAME = "username";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String EXPIRE_IN = "expire_in";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtProperties jwtProperties;

    /**
     * 生成 token 令牌
     *
     * @param userId 用户Id或用户名
     * @return 令token牌
     */
    public Map<String, Object> generateTokenAndRefreshToken(String userId, String username) {
        // 存放 accessToken、refreshToken 和 accessToken 过期时间
        Map<String, Object> tokenMap = new HashMap<>(3);

        // 从Redis缓存中获取token
        tokenMap = getTokenFromCache(userId);
        if (tokenMap != null) {
            return tokenMap;
        }

        // 在缓存中没找到，则重新生成 token
        tokenMap = buildToken(userId, username);

        // 缓存 token
        cacheToken(userId, tokenMap);
        return tokenMap;
    }

    public Map<String, Object> refreshTokenAndGenerateToken(String userId, String username) {
        Map<String, Object> tokenMap = buildToken(userId, username);
        stringRedisTemplate.delete(JWT_CACHE_KEY + userId);
        cacheToken(userId, tokenMap);

        return tokenMap;
    }

    /**
     * 从Redis缓存中获取token
     * @param userId
     * @return
     */
    public Map<String, Object> getTokenFromCache(String userId) {
        Map<String, Object> tokenMap = null;
        String accessToken = (String)stringRedisTemplate.opsForHash().get(JWT_CACHE_KEY + userId, ACCESS_TOKEN);
        String refreshToken = (String)stringRedisTemplate.opsForHash().get(JWT_CACHE_KEY + userId, REFRESH_TOKEN);
        if(!StringUtils.isEmpty(accessToken)) {
            tokenMap = new HashMap<>(3);
            tokenMap.put(ACCESS_TOKEN, accessToken);
            tokenMap.put(REFRESH_TOKEN, refreshToken);
            tokenMap.put(EXPIRE_IN, jwtProperties.getExpiration());
        }
        return tokenMap;
    }

    /**
     * 生成 token
     * @param userId
     * @param username
     * @return
     */
    private Map<String, Object> buildToken(String userId, String username) {
        String accessToken = generateToken(userId, username, null);
        String refreshToken = generateRefreshToken(userId, username, null);
        HashMap<String, Object> tokenMap = new HashMap<>(3);
        tokenMap.put(ACCESS_TOKEN, accessToken);
        tokenMap.put(REFRESH_TOKEN, refreshToken);
        tokenMap.put(EXPIRE_IN, jwtProperties.getExpiration());
        return tokenMap;
    }

    public String generateToken(String userId, String username, Map<String,String> payloads) {
        Map<String, Object> claims = buildClaims(userId, username, payloads);
        return generateToken(claims);
    }

    /**
     * 添加信息，构建Map
     * @param userId
     * @param username
     * @param payloads
     * @return
     */
    private Map<String, Object> buildClaims(String userId, String username, Map<String, String> payloads){
        int payloadSizes = payloads == null? 0 : payloads.size();
        Map<String, Object> claims = new HashMap<>(payloadSizes + 2);

        claims.put("sub", userId);
        claims.put("username", username);
        claims.put("created", new Date());
        if(payloadSizes > 0){
            claims.putAll(payloads);
        }

        return claims;
    }

    /**
     * 根据Map，生成token
     * @param claims
     * @return
     */
    private String generateToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + jwtProperties.getExpiration());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret())
                .compact();
    }

    public String generateRefreshToken(String userId, String username, Map<String,String> payloads) {
        Map<String, Object> claims = buildClaims(userId, username, payloads);
        return generateRefreshToken(claims);
    }

    /**
     * 生成刷新令牌 refreshToken，有效期是令牌的 2 倍
     * @param claims
     * @return
     */
    private String generateRefreshToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + jwtProperties.getExpiration() * 2);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret())
                .compact();
    }

    /**
     * 缓存到Redis中
     * @param userId
     * @param tokenMap
     */
    private void cacheToken(String userId, Map<String, Object> tokenMap) {
        String redisKey = JWT_CACHE_KEY + userId;
        stringRedisTemplate.opsForHash().put(redisKey, ACCESS_TOKEN, tokenMap.get(ACCESS_TOKEN));
        stringRedisTemplate.opsForHash().put(redisKey, REFRESH_TOKEN, tokenMap.get(REFRESH_TOKEN));
        stringRedisTemplate.expire(redisKey, jwtProperties.getExpiration(), TimeUnit.MILLISECONDS);
    }

//    public String getUserIdFromRequest(HttpServletRequest request) {
//        return request.getHeader(USER_ID);
//    }

    /**
     * 删除缓存
     * @param userId
     * @return
     */
    public boolean removeToken(String userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(JWT_CACHE_KEY + userId));
    }

    /**
     * 从 token 中获取数据声明
     * @param token
     * @return
     */
    private Claims getClaimsFromToken(String token){
        Claims claims;
        try{
            claims = Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    public String getUserIdFromToken(String token) {
        String userId;
        try {
            Claims claims = getClaimsFromToken(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            userId = null;
        }
        return userId;
    }

    public String getUserNameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = (String) claims.get(USER_NAME);
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 判断刷新令牌在不在 redis 中
     * @param token 刷新令牌
     * @return
     */
    public Boolean isRefreshTokenNotExistCache(String token) {
        String userId = getUserIdFromToken(token);
        String refreshToken = (String)stringRedisTemplate.opsForHash().get(JWT_CACHE_KEY + userId, REFRESH_TOKEN);
        return refreshToken == null || !refreshToken.equals(token);
    }

    /**
     * 判断令牌是否过期
     * @param token
     * @return
     */
    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            // 验证令牌失败 等同于 过期
            return true;
        }
    }

    /**
     * 刷新令牌
     * @param token
     * @return
     */
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            Claims claims = getClaimsFromToken(token);
            claims.put("created", new Date());
            refreshedToken = generateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    /**
     * 验证令牌
     * @param token
     * @param userId
     * @return
     */
    public Boolean validateToken(String token, String userId) {
        String id = getUserIdFromToken(token);
        return id.equals(userId) && !isTokenExpired(token);
    }

//    public String getUserIdFromRequest(HttpServletRequest request) {
//        return request.getHeader(USER_ID);
//    }

    public String getUserIdFromRequest(ServerHttpRequest request) {
        // 在ServerHttpRequest中，使用getHeaders().getFirst()来获取header
        return request.getHeaders().getFirst(USER_ID);
    }
}
