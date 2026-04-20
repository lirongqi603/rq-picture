package com.rq.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.cloudpicturebackend.constant.UserConstant;
import com.rq.cloudpicturebackend.enums.UserRoleEnum;
import com.rq.cloudpicturebackend.exception.BusinessException;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.manager.auth.StpKit;
import com.rq.cloudpicturebackend.model.dto.user.*;
import com.rq.cloudpicturebackend.model.entity.User;
import com.rq.cloudpicturebackend.model.vo.UserInfoVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.model.vo.UserQueryListVo;
import com.rq.cloudpicturebackend.service.UserService;
import com.rq.cloudpicturebackend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.rq.cloudpicturebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册参数
     * @return 用户id
     */
    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        //1.参数校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不能为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不能为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 16) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度不在4-16范围内");
        }
        if (userPassword.length() < 8 || userPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度不在8-16范围内");
        }
        if (checkPassword.length() < 8 || checkPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "确认密码长度不在8-16范围内");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入密码不一致");
        }
        //查询用户是否存在
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        long count = this.count(userQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户账号已存在");
        }
        //将用户信息存入数据库
        String encryptPassword = getEncryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setUserName("无名");
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * 密码加密
     *
     * @param password 密码
     * @return 加密后密码
     */
    @Override
    public String getEncryptPassword(String password) {
        final String salt = "lrq";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录参数
     * @return 用户信息
     */
    @Override
    public UserLoginVo userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        //校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码不能为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 16) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度不在4-16范围内");
        }
        if (userPassword.length() < 8 || userPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度不在8-16范围内");
        }
        //加密
        String encryptPassword = getEncryptPassword(userPassword);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", encryptPassword);
        User user = this.getOne(userQueryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }
        UserLoginVo userLoginVo = BeanUtil.copyProperties(user, UserLoginVo.class);
        //保存登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);
        return userLoginVo;
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 用户信息
     */
    @Override
    public UserLoginVo getLoginUser(HttpServletRequest request) {
        //获取登录态
        Object userLoginState = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userLoginState == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "未登录");
        }
        //转换为vo
        UserLoginVo vo = BeanUtil.copyProperties(userLoginState, UserLoginVo.class);
        if (vo.getId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "未登录");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id", vo.getId());
        //查询数据库中用户
        User user = this.getOne(userQueryWrapper);
        //用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "未登录");
        }
        return BeanUtil.copyProperties(user, UserLoginVo.class);
    }

    /**
     * 用户退出登录
     *
     * @param request 请求
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        //校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        StpKit.SPACE.logout(user.getId());
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    /**
     * 用户新增
     *
     * @param userAddRequest 新增请求
     * @return 新增结果
     */
    @Override
    public Boolean addUser(UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        User user = BeanUtil.copyProperties(userAddRequest, User.class);
        checkUser(user, false);
        return this.save(user);
    }

    /**
     * 用户修改
     *
     * @param userUpdateRequest 修改请求
     * @return 修改结果
     */
    @Override
    public Boolean updateUser(UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        User user = BeanUtil.copyProperties(userUpdateRequest, User.class);
        checkUser(user, true);
        return this.updateById(user);
    }

    /**
     * 用户列表查询
     *
     * @param page             分页
     * @param userQueryRequest 查询参数
     * @return 用户列表
     */
    @Override
    public IPage<UserQueryListVo> queryUserList(Page<User> page, UserQueryRequest userQueryRequest) {
        return userMapper.queryUserList(page, userQueryRequest);
    }

    /**
     * 获取用户详情
     *
     * @param id 用户id
     * @return 用户详情
     */
    @Override
    public UserInfoVo getUserInfoById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return BeanUtil.copyProperties(user, UserInfoVo.class);
    }

    @Override
    public boolean isAdmin(UserLoginVo loginUser) {
        return loginUser != null && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
    }

    /**
     * 验证用户参数
     *
     * @param user     用户
     * @param isUpdate 是否修改操作
     */
    private void checkUser(User user, boolean isUpdate) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }
        if (isUpdate) {
            Long id = user.getId();
            if (id == null || id < 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "用户id为空");
            }
        } else {
            user.setUserPassword(getEncryptPassword("12345678"));
        }
        String userAccount = user.getUserAccount();
        if (StrUtil.isBlank(userAccount)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户账号不能为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 16) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度不在4-16范围内");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        if (isUpdate) {
            userQueryWrapper.ne("id", user.getId());
        }
        long count = this.count(userQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户账号已存在");
        }
        String userName = user.getUserName();
        if (StrUtil.isBlank(userName)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户昵称不能为空");
        }
        if (userName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户昵称长度大于20");
        }
        String userAvatar = user.getUserAvatar();
        if (StrUtil.isNotBlank(userAvatar) && userAvatar.length() > 500) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户头像长度大于500");
        }
        String userProfile = user.getUserProfile();
        if (StrUtil.isNotBlank(userProfile) && userProfile.length() > 300) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户简介长度大于300");
        }
        String userRole = user.getUserRole();
        if (StrUtil.isBlank(userRole)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户角色不能为空");
        }
        if (UserRoleEnum.getEnumByCode(userRole) == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户角色不合法");
        }
    }
}