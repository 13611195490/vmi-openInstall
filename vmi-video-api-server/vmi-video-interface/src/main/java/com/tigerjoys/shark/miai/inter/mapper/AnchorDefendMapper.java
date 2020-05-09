package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.AnchorDefendEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  用户守护主播表[t_anchor_defend]表 dao通用操作接口实现类
 * @author shiming
 * @Date 2019-10-04 20:37:29
 *
 */
@Producer(entityType=AnchorDefendEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface AnchorDefendMapper extends BaseMapper<AnchorDefendEntity> {
    
}