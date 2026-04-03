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
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不能为空");
        }
        Long id = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(id);
    }

    @PostMapping("/login")
    public BaseResponse<UserLoginVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不能为空");
        }
        UserLoginVo userLoginVo = userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(userLoginVo);
    }

    @GetMapping("/get/login")
    public BaseResponse<UserLoginVo> getUserLogin(HttpServletRequest request) {
        UserLoginVo userLoginVo = userService.getLoginUser(request);
        return ResultUtils.success(userLoginVo);
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        userService.userLogout(request);
        return ResultUtils.success(true);
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> userAdd(@RequestBody UserAddRequest userAddRequest) {
        Boolean result = userService.addUser(userAddRequest);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> userUpdate(@RequestBody UserUpdateRequest userUpdateRequest) {
        Boolean result = userService.updateUser(userUpdateRequest);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/delete")
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<IPage<UserQueryListVo>> userList(@RequestBody UserQueryRequest userQueryRequest) {
        Page<User> page = new Page<>(userQueryRequest.getCurrent(), userQueryRequest.getPageSize());
        IPage<UserQueryListVo> pageList = userService.queryUserList(page, userQueryRequest);
        return ResultUtils.success(pageList);
    }

    @GetMapping("/getInfo/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserInfoVo> getInfo(@PathVariable("id") Long id) {
        UserInfoVo vo = userService.getUserInfoById(id);
        return ResultUtils.success(vo);
    }
}
