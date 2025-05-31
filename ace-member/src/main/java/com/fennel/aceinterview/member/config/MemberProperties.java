package com.fennel.aceinterview.member.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "member")
public class MemberProperties {
    private String nickname;
    private Integer age;

    // getters and setters
}
