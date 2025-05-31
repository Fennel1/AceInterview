package com.fennel.aceinterview.member.controller;

import com.fennel.aceinterview.member.config.MemberProperties;
import com.fennel.common.utils.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("member/sample")
public class SampleController {

    private final MemberProperties memberProperties;

    public SampleController(MemberProperties memberProperties) {
        this.memberProperties = memberProperties;
    }

    @RequestMapping("/test-local-config")
    public R testLocalConfig() {
        return R.ok()
                .put("nickname", memberProperties.getNickname())
                .put("age", memberProperties.getAge());
    }
}
