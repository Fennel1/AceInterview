package com.fennel.aceinterview.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.fennel.aceinterview.acejwt.utils.JwtTokenUtil;
import com.fennel.aceinterview.member.feign.StudyTimeFeignService;
import com.fennel.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fennel.aceinterview.member.entity.MemberEntity;
import com.fennel.aceinterview.member.service.MemberService;
import com.fennel.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.api.R;

import javax.annotation.Resource;


/**
 * 会员-会员表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:48:51
 */
@Slf4j
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private StudyTimeFeignService studyTimeFeignService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R<PageUtils> list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok(page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R<MemberEntity> info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok(member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R<MemberEntity> save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok(member);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R<Boolean> update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok(true);
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R<Boolean> delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok(true);
    }

    @RequestMapping("/studytime/list/test/{id}")
    public R<String> getMemberStudyTimeListTest(@PathVariable("id") Long id) {
        // mock数据库查到的会员信息
        MemberEntity memberEntity = new MemberEntity();
        // 学习时长：100分钟
        memberEntity.setId(id);
        memberEntity.setNickname("悟空聊架构");

        //远程调用拿到该用户的学习时长（学习时长是mock数据）
//        R memberStudyTimeList = studyTimeFeignService.getMemberStudyTimeListTest(id);
//        return R.ok().put("member", memberEntity).put("studyTime", memberStudyTimeList.get("studyTime"));
        return R.ok("");
    }


    /**
     * 通过网关拿到 token 中的 userId，然后根据 userId 查询用户信息
     * @return
     */
    @RequestMapping("/userinfo")
    public R<MemberEntity> info(){
        // 从线程里面拿，依赖自定义拦截器
        String userId = SecurityUtils.getUserId();
        log.info("MemberController userId:{}", userId);
        MemberEntity member = memberService.getMemberByUserId(userId);
        return R.ok(member);
    }
}
