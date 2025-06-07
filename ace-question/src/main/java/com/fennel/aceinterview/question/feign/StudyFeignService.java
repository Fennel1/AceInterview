package com.fennel.aceinterview.question.feign;

import com.fennel.common.to.study.ViewLog;
import com.fennel.common.utils.R;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Mapper
@FeignClient("ace-study")
public interface StudyFeignService {

    @PostMapping("study/viewlog/update")
    R update(@RequestBody ViewLog viewLog);
}
