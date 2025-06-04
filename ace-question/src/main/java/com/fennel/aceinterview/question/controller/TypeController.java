package com.fennel.aceinterview.question.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fennel.aceinterview.question.entity.TypeEntity;
import com.fennel.aceinterview.question.service.TypeService;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.R;



/**
 * 题目-题目类型表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@Slf4j
@RestController
@RequestMapping("question/type")
public class TypeController {
    @Autowired
    private TypeService typeService;

    // 本地缓存
    private Map<String, Object> cache = new HashMap<>();

    /**
     * 本地缓存查询类型列表
     * @param params
     * @return
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        // 查询本地缓存
        PageUtils page = (PageUtils) cache.get("typeEntityList");
        if (page == null) {
            log.info("本地缓存为空");
            // 从数据库中查询数据
            page = typeService.queryPage(params);
            cache.put("typeEntityList", page);
        }
        return R.ok().put("page", page);
    }

    /**
     * Redis 缓存查询类型列表
     * @param params
     * @return
     */
    @RequestMapping("/listByRedis")
    public R listByRedis(@RequestParam Map<String, Object> params){
        PageUtils page = typeService.queryPageRedis(params);
        return R.ok().put("page", page);
    }

    /**
     * 缓存击穿 加锁
     * @param params
     * @return
     */
    @RequestMapping("/listByRedisLock")
    public R listByRedisLock(@RequestParam Map<String, Object> params) {
        PageUtils page = typeService.queryPageRedisLock(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		TypeEntity type = typeService.getById(id);

        return R.ok().put("type", type);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody TypeEntity type){
		typeService.save(type);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody TypeEntity type){
		typeService.updateById(type);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		typeService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
