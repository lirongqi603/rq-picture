package com.rq.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.cloudpicturebackend.model.dto.user.*;
import com.rq.cloudpicturebackend.model.entity.User;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.model.vo.UserQueryListVo;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册参数
     * @return id
     */
    Long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 密码加密
     *
     * @param password 密码
     * @return 加密后密码
     */
    String getEncryptPassword(String password);

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录参数
     * @return 用户信息
     */
    UserLoginVo userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 用户信息
     */
    UserLoginVo getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录
     *
     * @param request 请求
     */
    void userLogout(HttpServletRequest request);

    /**
     * 用户新增
     *
     * @param userAddRequest 新增请求
     * @return 新增结果
     */
    Boolean addUser(UserAddRequest userAddRequest);

    /**
     * 用户修改
     *
     * @param userUpdateRequest 修改请求
     * @return 修改结果
     */
    Boolean updateUser(UserUpdateRequest userUpdateRequest);

    /**
     * 用户列表产线
     * @param page 分页
     * @param userQueryRequest 查询参数
     * @return 用户列表
     */
    IPage<UserQueryListVo> queryUserList(Page<User> page, UserQueryRequest userQueryRequest);
}
