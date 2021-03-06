package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.UserInformEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  用户举报表[t_user_inform]表 dao通用操作接口实现类
 * @author lipeng
 * @Date 2017-08-17 11:03:29
 *
 */
@Producer(entityType=UserInformEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface UserInformMapper extends BaseMapper<UserInformEntity> {
    
}