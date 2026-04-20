package com.rq.cloudpicturebackend.controller;

import com.rq.cloudpicturebackend.common.BaseResponse;
import com.rq.cloudpicturebackend.common.DeletedRequest;
import com.rq.cloudpicturebackend.common.ResultUtils;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.rq.cloudpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.rq.cloudpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.rq.cloudpicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.rq.cloudpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.rq.cloudpicturebackend.model.vo.SpaceUserVo;
import com.rq.cloudpicturebackend.model.vo.SpaceVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.SpaceUserService;
import com.rq.cloudpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    /**
     * 新增空间用户
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAM_ERROR);
        Boolean result = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除空间用户
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeletedRequest deletedRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deletedRequest == null, ErrorCode.PARAM_ERROR);
        UserLoginVo loginUser = userService.getLoginUser(request);
        Boolean result = spaceUserService.deleteSpaceUser(deletedRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 更新空间用户
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        ThrowUtils.throwIf(spaceUserEditRequest == null, ErrorCode.PARAM_ERROR);
        Boolean result = spaceUserService.editSpaceUser(spaceUserEditRequest);
        return ResultUtils.success(result);
    }

    /**
     * 查询空间用户
     */
    @PostMapping("/query")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVo>> querySpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAM_ERROR);
        List<SpaceUserVo> result = spaceUserService.querySpaceUser(spaceUserQueryRequest);
        return ResultUtils.success(result);
    }

    /**
     * 查询当前用户团队空间
     */
    @PostMapping("/myTeamSpace")
    public BaseResponse<List<SpaceVo>> myTeamSpace(HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        List<SpaceVo> result = spaceUserService.myTeamSpace(loginUser);
        return ResultUtils.success(result);
    }
}
