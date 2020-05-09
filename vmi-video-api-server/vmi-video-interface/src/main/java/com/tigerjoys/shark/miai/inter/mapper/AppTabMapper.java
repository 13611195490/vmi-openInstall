package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.AppTabEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  [t_app_tab]表 dao通用操作接口实现类
 * @author shiming
 * @Date 2018-12-25 10:49:36
 *
 */
@Producer(entityType=AppTabEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface AppTabMapper extends BaseMapper<AppTabEntity> {
    
}