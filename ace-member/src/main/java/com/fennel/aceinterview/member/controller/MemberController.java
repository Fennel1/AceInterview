package com.fennel.aceinterview.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.fennel.aceinterview.member.feign.StudyTimeFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fennel.aceinterview.member.entity.MemberEntity;
import com.fennel.aceinterview.member.service.MemberService;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.R;



/**
 * 会员-会员表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:48:51
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private StudyTimeFeignService studyTimeFeignService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @RequestMapping("/studytime/list/test/{id}")
    public R getMemberStudyTimeListTest(@PathVariable("id") Long id) {
        // mock数据库查到的会员信息
        MemberEntity memberEntity = new MemberEntity();
        // 学习时长：100分钟
        memberEntity.setId(id);
        memberEntity.setNickname("悟空聊架构");

        //远程调用拿到该用户的学习时长（学习时长是mock数据）
        R memberStudyTimeList = studyTimeFeignService.getMemberStudyTimeListTest(id);
        return R.ok().put("member", memberEntity).put("studyTime", memberStudyTimeList.get("studyTime"));
    }

}
