package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.TypeUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.swing.plaf.ListUI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryTypeList() {
        String key = RedisConstants.CACHE_SHOP_TYPE;
        //从redis中获取商铺类型数据
        List<String> list = stringRedisTemplate.opsForList().range(key,0, -1);
        //存在直接返回商铺类型数据
        if(list != null && list.size() > 0){
            List<ShopType> shopTypes = BeanUtil.copyToList(list, ShopType.class);
            return Result.ok(shopTypes);
        }
        //不存在,从数据库中查询数据
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        //不存在,返回商铺类型不存在
        if(shopTypes == null || shopTypes.size() < 0){
            return Result.fail("商铺类型不存在");
        }
        //存在,将数据写入redis中
        List<String> stringList = shopTypes.stream()
                .map(ShopType::getName)  // 使用 getName 方法
                .collect(Collectors.toList());
        stringRedisTemplate.opsForList().leftPushAll(key, stringList);
        //返回商铺类型数据
        return Result.ok(shopTypes);
    }
}
