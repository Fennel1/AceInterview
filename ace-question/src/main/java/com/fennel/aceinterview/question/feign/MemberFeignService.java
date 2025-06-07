package com.fennel.aceinterview.question.feign;

import com.fennel.common.to.member.GrowthChangeHistory;
import com.fennel.common.utils.R;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Mapper
@FeignClient("ace-member")
public interface MemberFeignService {

    @PostMapping("member/growthchangehistory/update")
    R update(@RequestBody GrowthChangeHistory growthChangeHistory);
}
