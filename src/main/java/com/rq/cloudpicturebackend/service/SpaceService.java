package com.rq.cloudpicturebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.cloudpicturebackend.common.BaseResponse;
import com.rq.cloudpicturebackend.common.DeletedRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceEditRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.cloudpicturebackend.model.vo.SpaceVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;

public interface SpaceService extends IService<Space> {

    Boolean addSpace(SpaceAddRequest spaceAddRequest, UserLoginVo loginUser);

    Boolean deleteSpace(DeletedRequest deletedRequest);

    Boolean updateSpace(SpaceUpdateRequest spaceUpdateRequest, UserLoginVo loginUser);

    Boolean editSpace(SpaceEditRequest spaceEditRequest, UserLoginVo loginUser);

    Page<Space> listPage(SpaceQueryRequest spaceQueryRequest);

    Page<SpaceVo> listPageVo(SpaceQueryRequest spaceQueryRequest, UserLoginVo loginUser);

    SpaceVo getSpaceVoById(Long id, UserLoginVo loginUser);

}
