package com.fennel.aceinterview.question.feign;

import com.fennel.common.to.es.QuestionEsModel;
import com.fennel.common.utils.R;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Mapper
@FeignClient("ace-search")
public interface SearchFeignService {

    @PostMapping("search/question/save")
    R saveQuestion(@RequestBody QuestionEsModel questionEsModel);
}
