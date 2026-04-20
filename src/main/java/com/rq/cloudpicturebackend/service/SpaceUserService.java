package com.rq.cloudpicturebackend.service;

import com.rq.cloudpicturebackend.common.DeletedRequest;
import com.rq.cloudpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.rq.cloudpicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.rq.cloudpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.rq.cloudpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.cloudpicturebackend.model.vo.SpaceUserVo;
import com.rq.cloudpicturebackend.model.vo.SpaceVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;

import java.util.List;

/**
 *
 */
public interface SpaceUserService extends IService<SpaceUser> {

    Boolean addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    Boolean deleteSpaceUser(DeletedRequest deletedRequest, UserLoginVo loginUser);

    Boolean editSpaceUser(SpaceUserEditRequest spaceUserEditRequest);

    List<SpaceUserVo> querySpaceUser(SpaceUserQueryRequest spaceUserQueryRequest);

    List<SpaceVo> myTeamSpace(UserLoginVo loginUser);

}
