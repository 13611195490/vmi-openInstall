package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.AnchorToUserEvaluationLogEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  主播对用户评价日志[t_anchor_to_user_evaluation_log]表 dao通用操作接口实现类
 * @author yangjunming
 * @Date 2019-06-26 20:24:04
 *
 */
@Producer(entityType=AnchorToUserEvaluationLogEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface AnchorToUserEvaluationLogMapper extends BaseMapper<AnchorToUserEvaluationLogEntity> {
    
}