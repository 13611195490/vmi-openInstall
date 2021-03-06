package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.StatisticsUserTotalEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  [t_statistics_user_total]表 dao通用操作接口实现类
 * @author chengang
 * @Date 2017-05-26 15:03:43
 *
 */
@Producer(entityType=StatisticsUserTotalEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface StatisticsUserTotalMapper extends BaseMapper<StatisticsUserTotalEntity> {
    
}