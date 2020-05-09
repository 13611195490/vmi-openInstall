package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.UserCharmEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  用户魅力值表[t_user_charm]表 dao通用操作接口实现类
 * @author lipeng
 * @Date 2017-08-24 15:42:34
 *
 */
@Producer(entityType=UserCharmEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface UserCharmMapper extends BaseMapper<UserCharmEntity> {
    
}