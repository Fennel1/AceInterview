package com.fennel.aceinterview.acegateway.filter;

import com.alibaba.fastjson.JSON;
import com.fennel.aceinterview.acejwt.common.ResponseCodeEnum;
import com.fennel.aceinterview.acejwt.common.ResponseResult;
import com.fennel.aceinterview.acejwt.config.JwtProperties;
import com.fennel.aceinterview.acejwt.constants.JwtTokenConstants;
import com.fennel.aceinterview.acejwt.utils.JwtTokenUtil;
import com.fennel.common.constant.Constants;
import com.fennel.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@Component
public class JwtAuthCheckFilter extends AbstractGatewayFilterFactory<Object> {
    private static final String WECHAT_URL_REGEX = "/*/app/";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "username";
    public static final String FROM_SOURCE = "from-source";

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest serverHttpRequest = exchange.getRequest();
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            // 由于 ServerHttpRequest 是不可变的，如果你需要基于现有请求创建一个修改过的新请求，就不能直接修改原始对象
            ServerHttpRequest.Builder mutate = serverHttpRequest.mutate();
            String requestUrl = serverHttpRequest.getURI().getPath();

            // 判断是否跳过 url
            if (isSkipValidUrl(requestUrl)) {
                return chain.filter(exchange);
            }

            // 从 HTTP 请求头中获取 JWT 令牌
            String token = getToken(serverHttpRequest);
            if (StringUtils.isEmpty(token)) {
                return unauthorizedResponse(exchange, serverHttpResponse, ResponseCodeEnum.TOKEN_MISSION);
            }

            // 对Token解签名，并验证Token是否过期
            boolean isJwtNotValid = jwtTokenUtil.isTokenExpired(token);
            if (isJwtNotValid) {
                return unauthorizedResponse(exchange, serverHttpResponse, ResponseCodeEnum.TOKEN_INVALID);
            }

            // 验证 token 里面的 userId 是否为空
            String userId = jwtTokenUtil.getUserIdFromToken(token);
            String username = jwtTokenUtil.getUserNameFromToken(token);
            if (StringUtils.isEmpty(userId)) {
                return unauthorizedResponse(exchange, serverHttpResponse, ResponseCodeEnum.TOKEN_CHECK_INFO_FAILED);
            }

            log.info("userId:{}, username:{}", userId, username);
            addHeader(mutate, USER_ID, userId);
            addHeader(mutate, USER_NAME, username);
            removeHeader(mutate, FROM_SOURCE);
            return chain.filter(exchange.mutate().request(mutate.build()).build());
        };
    }

    /**
     * 是否跳过对请求 url 的 token 校验
     * @param requestUrl
     * @return
     */
    private boolean isSkipValidUrl(String requestUrl) {
        boolean skip = false;
        String skipValidUrl = jwtProperties.getSkipValidUrl();
        if (!StringUtils.isEmpty(skipValidUrl)) {
            String[] skipValidUrls = skipValidUrl.split(",");
            skip = Arrays.stream(skipValidUrls).map(String::trim).anyMatch(url -> url.equals(requestUrl));
        }
        return skip;
    }

    /**
     * 获取请求token
     * @param request
     * @return
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(jwtProperties.getHeader());
        if (!StringUtils.isEmpty(token) && token.startsWith(JwtTokenConstants.PREFIX)) {
            token = token.replaceFirst(JwtTokenConstants.PREFIX, StringUtils.EMPTY);
        }
        log.info("token:{}", token);
//        if (token == null || token.isEmpty()) {
//            token = request.getHeaders().getFirst("X-Token");
//        }
        return token;
    }

    /**
     * 将 JWT 鉴权失败的消息响应给客户端
     * @param exchange
     * @param serverHttpResponse
     * @param responseCodeEnum
     * @return
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange,
                                            ServerHttpResponse serverHttpResponse,
                                            ResponseCodeEnum responseCodeEnum) {
        log.error("[鉴权异常处理]请求路径:{}", exchange.getRequest().getPath());
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        ResponseResult responseResult = ResponseResult.error(responseCodeEnum.getCode(), responseCodeEnum.getMessage());
        DataBuffer dataBuffer = serverHttpResponse.bufferFactory()
                .wrap(JSON.toJSONStringWithDateFormat(responseResult, JSON.DEFFAULT_DATE_FORMAT)
                        .getBytes(StandardCharsets.UTF_8));
        return serverHttpResponse.writeWith(Flux.just(dataBuffer));
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = urlEncode(valueStr);
        mutate.header(name, valueEncode);
    }

    public String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, Constants.UTF8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private void removeHeader(ServerHttpRequest.Builder mutate, String name) {
        mutate.headers(httpHeaders -> httpHeaders.remove(name)).build();
    }
}
