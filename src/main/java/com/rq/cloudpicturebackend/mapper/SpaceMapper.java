package com.rq.cloudpicturebackend.mapper;

import com.rq.cloudpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.rq.cloudpicturebackend.model.entity.Space
 */
public interface SpaceMapper extends BaseMapper<Space> {

    void calculateSpaceUsage(@Param("id") Long spaceId);

}




