package com.fennel.aceinterview.member.feign;

import com.fennel.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("ace-study")
public interface StudyTimeFeignService {

    @RequestMapping("study/studytime/member/list/test/{id}")
    public R getMemberStudyTimeListTest(@PathVariable("id") Long id);
}
