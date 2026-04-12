package com.rq.cloudpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.cloudpicturebackend.annotation.AuthCheck;
import com.rq.cloudpicturebackend.common.BaseResponse;
import com.rq.cloudpicturebackend.common.DeletedRequest;
import com.rq.cloudpicturebackend.common.ResultUtils;
import com.rq.cloudpicturebackend.constant.UserConstant;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceEditRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.vo.SpaceVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.SpaceService;
import com.rq.cloudpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Description: 空间管理
 * @ClassName: SpaceController
 * @Author: lirongqi
 * @Date: 2026-04-12
 */
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    /**
     * 新增空间
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        Boolean result = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除空间
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeletedRequest deletedRequest) {
        Boolean result = spaceService.deleteSpace(deletedRequest);
        return ResultUtils.success(result);
    }

    /**
     * 修改空间
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        Boolean result = spaceService.updateSpace(spaceUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 用户编辑空间
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        Boolean result = spaceService.editSpace(spaceEditRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 查询空间
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        Page<Space> result = spaceService.listPage(spaceQueryRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户查询空间
     */
    @PostMapping("/list/pageVo")
    public BaseResponse<Page<SpaceVo>> listPageVo(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        Page<SpaceVo> result = spaceService.listPageVo(spaceQueryRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 查询空间详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAM_ERROR);
        Space result = spaceService.getById(id);
        return ResultUtils.success(result);
    }

    /**
     * 用户查询空间详情
     */
    @GetMapping("/getVo")
    public BaseResponse<SpaceVo> getSpaceVoById(Long id, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAM_ERROR);
        SpaceVo result = spaceService.getSpaceVoById(id, loginUser);
        return ResultUtils.success(result);
    }
}
