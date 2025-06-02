package com.fennel.aceinterview.aceauth.controller;


import com.fennel.aceinterview.aceauth.jpa.SysUser;
import com.fennel.aceinterview.aceauth.jpa.SysUserRepository;
import com.fennel.aceinterview.acejwt.common.ResponseCodeEnum;
import com.fennel.aceinterview.acejwt.common.ResponseResult;
import com.fennel.aceinterview.acejwt.config.JwtProperties;
import com.fennel.aceinterview.acejwt.utils.JwtTokenUtil;
import com.baomidou.mybatisplus.extension.api.R;
import com.fennel.common.utils.SecurityUtils;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class JwtAuthController {

    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private SysUserRepository sysUserRepository;
    @Resource
    private JwtTokenUtil jwtTokenUtil;
//    @Resource
//    private PasswordEncoder passwordEncoder;

    /**
     * 使用用户名密码换 JWT 令牌
     * @param map
     * @return
     */
    @PostMapping("/login")
    public R<?> login(@RequestBody Map<String,String> map){
        String userId  = map.get(jwtProperties.getUserParamName());
        String password = map.get(jwtProperties.getPwdParamName());

        // 用户或密码为空
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(password)){
            return R.failed(ResponseCodeEnum.LOGIN_EMPTY_ERROR.getMessage());
        }

        SysUser sysUser = sysUserRepository.findByUserId(userId);
        if(sysUser != null) {
            boolean isAuthenticated = SecurityUtils.matchesPassword(password, sysUser.getPassword());
            //密码正确
            if (isAuthenticated){
                Map<String, Object> tokenMap = jwtTokenUtil.generateTokenAndRefreshToken(userId, sysUser.getUsername());
                return R.ok(tokenMap);
            }
            return R.failed(ResponseCodeEnum.LOGIN_ERROR.getMessage());
        }
        // 没有该用户
        return R.failed(ResponseCodeEnum.LOGIN_ERROR.getMessage());
    }

    /**
     * 刷新JWT令牌,用旧的令牌换新的令牌
     */
    @GetMapping("/refreshtoken")
    public Mono<ResponseResult> refreshToken(@RequestHeader("${aceinterview.jwt.header}") String token){
        token = SecurityUtils.replaceTokenPrefix(token);
        if (StringUtils.isEmpty(token)) {
            return buildErrorResponse(ResponseCodeEnum.TOKEN_MISSION);
        }

        // 验证 Token 是否过期
        boolean isJwtNotValid = jwtTokenUtil.isTokenExpired(token);
        if(isJwtNotValid){
            return buildErrorResponse(ResponseCodeEnum.TOKEN_INVALID);
        }

        //验证token中的 userid是否为空
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        String username = jwtTokenUtil.getUserNameFromToken(token);
        if (StringUtils.isEmpty(userId)) {
            return buildErrorResponse(ResponseCodeEnum.TOKEN_CHECK_INFO_FAILED);
        }

        //为了保证 refreshToken 只能用一次，刷新后，会从 redis 中删除。
        //如果用的不是 redis 中的 refreshToken 进行刷新令牌，则不能刷新。
        //如果使用 redis 中已过期的 refreshToken 也不能刷新令牌。
        boolean isRefreshTokenNotExisted = jwtTokenUtil.isRefreshTokenNotExistCache(token);
        if(isRefreshTokenNotExisted){
            return buildErrorResponse(ResponseCodeEnum.REFRESH_TOKEN_INVALID);
        }

        String us = jwtTokenUtil.getUserIdFromToken(token);
        Map<String, Object> tokenMap = jwtTokenUtil.refreshTokenAndGenerateToken(userId, username);
        return buildSuccessResponse(tokenMap);
    }

    /**
     * 登出，删除 redis 中的 accessToken 和 refreshToken
     * 只保证 refreshToken 不能使用，accessToken 还是能使用的。
     * 如果用户拿到了之前的 accessToken，则可以一直使用到过期，但是因为 refreshToken 已经无法使用了，所以保证了 accessToken 的时效性。
     * 下次登录时，需要重新获取新的 accessToken 和 refreshToken，这样才能利用 refreshToken 进行续期。
     * @param username
     * @return
     */
    @PostMapping("/logout")
    public Mono<ResponseResult> logout(@RequestParam("username") String username){
        boolean logoutResult = jwtTokenUtil.removeToken(username);
//        if (logoutResult) {
//            buildSuccessResponse(ResponseCodeEnum.SUCCESS);
//        } else {
//            buildErrorResponse(ResponseCodeEnum.LOGOUT_ERROR);
//        }
        return buildSuccessResponse(ResponseCodeEnum.SUCCESS);
    }

    private Mono<ResponseResult> buildErrorResponse(ResponseCodeEnum responseCodeEnum){
        return Mono.create(callback -> callback.success(
                ResponseResult.error(responseCodeEnum.getCode(), responseCodeEnum.getMessage())
        ));
    }

    private Mono<ResponseResult> buildSuccessResponse(Object data){
        return Mono.create(callback -> callback.success(ResponseResult.success(data)
        ));
    }
}
