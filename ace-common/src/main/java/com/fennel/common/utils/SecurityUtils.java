package com.fennel.common.utils;

import com.fennel.common.constant.TokenConstants;
import com.fennel.common.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.http.HttpServletRequest;

/**
 * 权限获取工具类
 *
 */
public class SecurityUtils {
    /**
     * 获取用户ID
     */
    public static String getUserId(){
        return SecurityContextHolder.getUserId();
    }

    public static String getUserName() {
        return SecurityContextHolder.getUserName();
    }

    /**
     * 根据request获取请求token
     * @param request
     * @return
     */
    public static String getToken(HttpServletRequest request) {
        // 从header获取token标识
        String token = request.getHeader(TokenConstants.AUTHENTICATION);
        return replaceTokenPrefix(token);
    }

    /**
     * 裁剪token前缀
     * @param token
     * @return
     */
    public static String replaceTokenPrefix(String token) {
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX)) {
            token = token.replaceFirst(TokenConstants.PREFIX, "");
        }
        return token;
    }

    /**
     * 生成BCryptPasswordEncoder密码
     * @param password
     * @return
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     * @param rawPassword
     * @param encodedPassword
     * @return
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
