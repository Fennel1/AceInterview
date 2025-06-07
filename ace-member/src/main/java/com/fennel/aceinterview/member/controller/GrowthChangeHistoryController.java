package com.fennel.aceinterview.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.fennel.common.to.member.GrowthChangeHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fennel.aceinterview.member.entity.GrowthChangeHistoryEntity;
import com.fennel.aceinterview.member.service.GrowthChangeHistoryService;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.R;



/**
 * 会员-积分值变化历史记录表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:48:51
 */
@RestController
@RequestMapping("member/growthchangehistory")
public class GrowthChangeHistoryController {
    @Autowired
    private GrowthChangeHistoryService growthChangeHistoryService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = growthChangeHistoryService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		GrowthChangeHistoryEntity growthChangeHistory = growthChangeHistoryService.getById(id);

        return R.ok().put("growthChangeHistory", growthChangeHistory);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody GrowthChangeHistoryEntity growthChangeHistory){
		growthChangeHistoryService.save(growthChangeHistory);

        return R.ok();
    }

    /**
     * 修改
     */
//    @RequestMapping("/update")
//    public R update(@RequestBody GrowthChangeHistoryEntity growthChangeHistory){
//		growthChangeHistoryService.updateById(growthChangeHistory);
//
//        return R.ok();
//    }
    @PostMapping("/update")
    public R update(@RequestBody GrowthChangeHistory growthChangeHistory) {
        growthChangeHistoryService.update(growthChangeHistory);
        return R.ok();
    }



    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		growthChangeHistoryService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
