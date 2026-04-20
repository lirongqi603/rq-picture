package com.rq.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.cloudpicturebackend.common.DeletedRequest;
import com.rq.cloudpicturebackend.enums.SpaceRoleEnum;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.mapper.SpaceUserMapper;
import com.rq.cloudpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.rq.cloudpicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.rq.cloudpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.entity.SpaceUser;
import com.rq.cloudpicturebackend.model.entity.User;
import com.rq.cloudpicturebackend.model.vo.SpaceUserVo;
import com.rq.cloudpicturebackend.model.vo.SpaceVo;
import com.rq.cloudpicturebackend.model.vo.UserInfoVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.SpaceService;
import com.rq.cloudpicturebackend.service.SpaceUserService;
import com.rq.cloudpicturebackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Override
    public Boolean addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAM_ERROR);
        String spaceRole = spaceUserAddRequest.getSpaceRole();
        ThrowUtils.throwIf(ObjUtil.isEmpty(spaceRole), ErrorCode.PARAM_ERROR, "角色不能为空");
        SpaceRoleEnum enumByValue = SpaceRoleEnum.getEnumByValue(spaceRole);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAM_ERROR, "无效的角色");
        Long spaceId = spaceUserAddRequest.getSpaceId();
        ThrowUtils.throwIf(ObjUtil.isEmpty(spaceId), ErrorCode.PARAM_ERROR, "空间id不能为空");
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.PARAM_ERROR, "空间不存在");
        Long userId = spaceUserAddRequest.getUserId();
        ThrowUtils.throwIf(ObjUtil.isEmpty(userId), ErrorCode.PARAM_ERROR, "空间用户不存在");
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAM_ERROR, "用户不存在");
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(userId);
        spaceUser.setSpaceRole(spaceRole);
        return save(spaceUser);
    }

    @Override
    public Boolean deleteSpaceUser(DeletedRequest deletedRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(deletedRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Long id = deletedRequest.getId();
        ThrowUtils.throwIf(ObjUtil.isEmpty(id), ErrorCode.PARAM_ERROR, "id不能为空");
        SpaceUser spaceUser = this.getById(id);
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAM_ERROR, "空间用户不存在");
        Long spaceId = spaceUser.getSpaceId();
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.PARAM_ERROR, "空间不存在 ");
        Long userId = space.getUserId();
        ThrowUtils.throwIf(userId.equals(spaceUser.getUserId()), ErrorCode.NOT_AUTH_ERROR, "空间创建人不能移除空间");
        return removeById(id);
    }

    @Override
    public Boolean editSpaceUser(SpaceUserEditRequest spaceUserEditRequest) {
        ThrowUtils.throwIf(spaceUserEditRequest == null, ErrorCode.PARAM_ERROR);
        Long id = spaceUserEditRequest.getId();
        ThrowUtils.throwIf(ObjUtil.isEmpty(id), ErrorCode.PARAM_ERROR, "id不能为空");
        String spaceRole = spaceUserEditRequest.getSpaceRole();
        ThrowUtils.throwIf(ObjUtil.isEmpty(spaceRole), ErrorCode.PARAM_ERROR, "角色不能为空");
        SpaceRoleEnum enumByValue = SpaceRoleEnum.getEnumByValue(spaceRole);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAM_ERROR, "无效的角色");
        SpaceUser spaceUser = this.getById(id);
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAM_ERROR, "空间用户不存在");
        Long spaceId = spaceUser.getSpaceId();
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.PARAM_ERROR, "空间不存在 ");
        Long userId = space.getUserId();
        if (userId.equals(spaceUser.getUserId())) {
            ThrowUtils.throwIf(!SpaceRoleEnum.ADMIN.getValue().equals(spaceRole), ErrorCode.NOT_AUTH_ERROR, "空间创建人角色不能修改");
        }
        spaceUser.setSpaceRole(spaceRole);
        return updateById(spaceUser);
    }

    @Override
    public List<SpaceUserVo> querySpaceUser(SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAM_ERROR);
        Long id = spaceUserQueryRequest.getId();
        Long sid = spaceUserQueryRequest.getSpaceId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(sid), "spaceId", sid);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        List<SpaceUser> list = this.list(queryWrapper);
        if (CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        List<Long> userIdList = list.stream().map(SpaceUser::getUserId).distinct().collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIdList);
        Map<Long, UserInfoVo> userInfoVoMap = userList.stream().map(data -> BeanUtil.copyProperties(data, UserInfoVo.class)).collect(Collectors.toMap(UserInfoVo::getId, user -> user));
        List<Long> spaceIdList = list.stream().map(SpaceUser::getSpaceId).distinct().collect(Collectors.toList());
        List<Space> spaceList = spaceService.listByIds(spaceIdList);
        Map<Long, SpaceVo> spaceVoMap = spaceList.stream().map(data -> BeanUtil.copyProperties(data, SpaceVo.class)).collect(Collectors.toMap(SpaceVo::getId, space -> space));
        return list.stream().map(data -> {
            SpaceUserVo spaceUserVo = BeanUtil.copyProperties(data, SpaceUserVo.class);
            Long spaceId = data.getSpaceId();
            SpaceVo spaceVo = spaceVoMap.get(spaceId);
            if (spaceVo != null) {
                spaceUserVo.setSpace(spaceVo);
            }
            Long userId = data.getUserId();
            UserInfoVo userInfoVo = userInfoVoMap.get(userId);
            if (userInfoVo != null) {
                spaceUserVo.setUser(userInfoVo);
            }
            return spaceUserVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceVo> myTeamSpace(UserLoginVo loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<SpaceUser> list = this.list(queryWrapper);
        List<Long> spaceIdList = list.stream().map(SpaceUser::getSpaceId).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(spaceIdList)) {
            return Collections.emptyList();
        }
        List<Space> spaceList = spaceService.listByIds(spaceIdList);
        return spaceList.stream().map(data -> BeanUtil.copyProperties(data, SpaceVo.class)).collect(Collectors.toList());
    }
}




