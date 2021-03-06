package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.TalentVipCategoryEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  达人VIP字典表[t_talent_vip_category]表 dao通用操作接口实现类
 * @author chengang
 * @Date 2017-08-21 08:55:32
 *
 */
@Producer(entityType=TalentVipCategoryEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface TalentVipCategoryMapper extends BaseMapper<TalentVipCategoryEntity> {
    
}