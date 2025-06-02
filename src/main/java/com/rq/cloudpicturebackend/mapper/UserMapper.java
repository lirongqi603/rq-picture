package com.rq.cloudpicturebackend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.cloudpicturebackend.model.dto.user.UserQueryRequest;
import com.rq.cloudpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rq.cloudpicturebackend.model.vo.UserQueryListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.rq.cloudpicturebackend.model.entity.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    IPage<UserQueryListVo> queryUserList(Page<User> page, @Param("userQueryRequest") UserQueryRequest userQueryRequest);

}




