package com.rq.cloudpicturebackend.aop;

import cn.hutool.core.util.StrUtil;
import com.rq.cloudpicturebackend.annotation.AuthCheck;
import com.rq.cloudpicturebackend.enums.UserRoleEnum;
import com.rq.cloudpicturebackend.exception.BusinessException;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限拦截器
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行权限校验
     *
     * @param joinPoint 连接点
     * @param authCheck 权限校验注解
     * @return 执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(authCheck)")
    public Object doCheckAuth(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        //获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        UserLoginVo userLoginVo = userService.getLoginUser(request);
        // 校验权限
        if (userLoginVo == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "未登录");
        }
        //获取需要的角色
        String mustRole = authCheck.mustRole();
        //校验-任何角色都有权限
        if (StrUtil.isBlank(mustRole)) {
            return joinPoint.proceed();

        }
        String userRole = userLoginVo.getUserRole();
        //校验-用户角色是否满足
        if (UserRoleEnum.ADMIN.getValue().equals(mustRole) && !mustRole.equals(userRole)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "权限不足");
        }
        return joinPoint.proceed();
    }


}
