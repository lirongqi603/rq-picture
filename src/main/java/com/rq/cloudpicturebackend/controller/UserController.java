package com.rq.cloudpicturebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.cloudpicturebackend.annotation.AuthCheck;
import com.rq.cloudpicturebackend.common.BaseResponse;
import com.rq.cloudpicturebackend.common.DeletedRequest;
import com.rq.cloudpicturebackend.common.ResultUtils;
import com.rq.cloudpicturebackend.constant.UserConstant;
import com.rq.cloudpicturebackend.exception.BusinessException;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.model.dto.user.*;
import com.rq.cloudpicturebackend.model.entity.User;
import com.rq.cloudpicturebackend.model.vo.UserInfoVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.model.vo.UserQueryListVo;
import com.rq.cloudpicturebackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不能为空");
        }
        Long id = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(id);
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", response = UserLoginVo.class)
    public BaseResponse<UserLoginVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不能为空");
        }
        UserLoginVo userLoginVo = userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(userLoginVo);
    }

    @GetMapping("/get/login")
    @ApiOperation(value = "获取当前登录用户信息", response = UserLoginVo.class)
    public BaseResponse<UserLoginVo> getUserLogin(HttpServletRequest request) {
        UserLoginVo userLoginVo = userService.getLoginUser(request);
        return ResultUtils.success(userLoginVo);
    }

    @PostMapping("/logout")
    @ApiOperation(value = "用户退出登录")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        userService.userLogout(request);
        return ResultUtils.success(true);
    }

    @PostMapping("/add")
    @ApiOperation(value = "用户新增")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> userAdd(@RequestBody UserAddRequest userAddRequest) {
        Boolean result = userService.addUser(userAddRequest);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @ApiOperation(value = "用户修改")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> userUpdate(@RequestBody UserUpdateRequest userUpdateRequest) {
        Boolean result = userService.updateUser(userUpdateRequest);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "用户删除")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> userDelete(@RequestBody DeletedRequest deletedRequest) {
        if (deletedRequest == null || deletedRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数错误");
        }
        boolean result = userService.removeById(deletedRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/list")
    @ApiOperation(value = "用户列表", response = UserQueryListVo.class)
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<IPage<UserQueryListVo>> userList(@RequestBody UserQueryRequest userQueryRequest) {
        Page<User> page = new Page<>(userQueryRequest.getCurrent(), userQueryRequest.getPageSize());
        IPage<UserQueryListVo> pageList = userService.queryUserList(page, userQueryRequest);
        return ResultUtils.success(pageList);
    }

    @GetMapping("/getInfo/{id}")
    @ApiOperation(value = "获取用户详情", response = UserInfoVo.class)
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserInfoVo> getInfo(@PathVariable("id") Long id) {
        UserInfoVo vo = userService.getUserInfoById(id);
        return ResultUtils.success(vo);
    }
}
