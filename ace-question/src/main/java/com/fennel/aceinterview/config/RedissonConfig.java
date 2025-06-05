package com.fennel.aceinterview.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.database:0}") // Default to database 0 if not specified
    private int database;

    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        // 1.创建配置
        Config config = new Config();
        String redisAddress = "redis://" + host + ":" + port;
        config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(database);

        if (password != null && !password.isEmpty()) {
            config.useSingleServer().setPassword(password);
        }

        return Redisson.create(config);
    }
}
