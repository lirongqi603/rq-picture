package com.rq.cloudpicturebackend.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.rq.cloudpicturebackend.enums.SpaceRoleEnum;
import com.rq.cloudpicturebackend.enums.SpaceTypeEnum;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.manager.auth.model.SpaceUserAuthContext;
import com.rq.cloudpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.rq.cloudpicturebackend.manager.auth.model.SpaceUserRole;
import com.rq.cloudpicturebackend.model.entity.Picture;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.entity.SpaceUser;
import com.rq.cloudpicturebackend.model.entity.User;
import com.rq.cloudpicturebackend.model.vo.UserInfoVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.PictureService;
import com.rq.cloudpicturebackend.service.SpaceService;
import com.rq.cloudpicturebackend.service.SpaceUserService;
import com.rq.cloudpicturebackend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.rq.cloudpicturebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {


    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private SpaceUserService spaceUserService;
    @Resource
    private PictureService pictureService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> emptyList = new ArrayList<>();
        //1.只对空间体系增加校验
        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            return emptyList;
        }
        //全部权限
        List<String> ADMIN_PERMISSION = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        List<String> VIEWER_PERMISSION = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.VIEWER.getValue());
        List<String> PRIVATE_PERMISSION = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.PRIVATE.getValue());
        List<String> EDITOR_PERMISSION = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.EDITOR.getValue());
        UserLoginVo loginUser = BeanUtil.toBean(StpKit.SPACE.getSessionByLoginId(loginId).getDataMap().get(USER_LOGIN_STATE), UserLoginVo.class);
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        boolean allNull = isAllFieldsNull(authContext);
        //4.如果所有的参数都为空，公共图库列表查询
        if (allNull && loginUser == null) {
            return VIEWER_PERMISSION;
        }
        //3.如果当前用户为管理员或全部参数为空，则拥有全部权限
        if (allNull || userService.isAdmin(loginUser)) {
            return ADMIN_PERMISSION;
        }
        //5.如果当前用户为普通用户，则根据权限判断
        //5.1操作团队空间用户，只允许空间管理员操作
        Long spaceUserId = authContext.getSpaceUserId();
        if (ObjUtil.isNotNull(spaceUserId)) {
            if(loginUser == null){
                return emptyList;
            }
            SpaceUser spaceUser = spaceUserService.getById(spaceUserId);
            ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
            Long spaceId = spaceUser.getSpaceId();
            boolean exists = spaceUserService.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId)
                    .eq(SpaceUser::getUserId, loginUser.getId())
                    .in(SpaceUser::getSpaceRole, SpaceRoleEnum.ADMIN.getValue()).exists();
            if (exists) {
                //返回团队成员管理权限
                return ADMIN_PERMISSION;
            } else {
                return emptyList;
            }
        }
        //5.2操作团队空间，只允许空间管理员和编辑操作，操作私有空间只允许本人操作
        Long spaceId = authContext.getSpaceId();
        if (ObjUtil.isNotNull(spaceId)) {
            if(loginUser == null){
                return emptyList;
            }
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            Integer spaceType = space.getSpaceType();
            if (SpaceTypeEnum.PRIVATE.getValue() == spaceType) {
                //私有空间，判断是否为本人
                Long userId = space.getUserId();
                if (userId.equals(loginUser.getId())) {
                    return PRIVATE_PERMISSION;
                } else {
                    return emptyList;
                }
            } else {
                SpaceUser spaceUser = spaceUserService.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId)
                        .eq(SpaceUser::getUserId, loginUser.getId()).one();
                if (spaceUser == null) {
                    return emptyList;
                }
                if (SpaceRoleEnum.ADMIN.getValue().equals(spaceUser.getSpaceRole())) {
                    return ADMIN_PERMISSION;
                } else if (SpaceRoleEnum.EDITOR.getValue().equals(spaceUser.getSpaceRole())) {
                    return EDITOR_PERMISSION;
                } else {
                    return VIEWER_PERMISSION;
                }
            }
        }
        //5.3 操作图库
        //5.3.1 图片类型：公共图库，私有图库，团队空间图库
        //5.3.2 公共图库: 只允许图片的创建人修改，其他人只能查看
        //5.3.3 私有图库: 只允许图片的创建人修改，其他人看不到
        //5.3.4 团队空间图库: 只允许空间管理员和编辑者修改，浏览者能查看，其他人看不到
        Long pictureId = authContext.getPictureId();
        if (ObjUtil.isNotNull(pictureId)) {
            Picture picture = pictureService.getById(pictureId);
            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
            Long sid = picture.getSpaceId();
            if (sid == null) {
                //如果spaceId为空，则表示该图片为公共图库
                if (loginUser != null && picture.getUserId().equals(loginUser.getId())) {
                    return PRIVATE_PERMISSION;
                } else {
                    return VIEWER_PERMISSION;
                }
            }
            Space space = spaceService.getById(sid);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            Integer spaceType = space.getSpaceType();
            if (SpaceTypeEnum.PRIVATE.getValue() == spaceType) {
                //私有空间，判断是否为本人
                Long userId = space.getUserId();
                if (loginUser != null && userId.equals(loginUser.getId())) {
                    return PRIVATE_PERMISSION;
                } else {
                    return emptyList;
                }
            } else {
                SpaceUser spaceUser = spaceUserService.lambdaQuery().eq(SpaceUser::getSpaceId, sid)
                        .eq(SpaceUser::getUserId, loginUser.getId()).one();
                if (spaceUser == null) {
                    return emptyList;
                }
                if (SpaceRoleEnum.ADMIN.getValue().equals(spaceUser.getSpaceRole())) {
                    return ADMIN_PERMISSION;
                } else if (SpaceRoleEnum.EDITOR.getValue().equals(spaceUser.getSpaceRole())) {
                    return EDITOR_PERMISSION;
                } else {
                    return VIEWER_PERMISSION;
                }
            }
        }
        return emptyList;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 从请求中获取上下文对象
     */
    private SpaceUserAuthContext getAuthContextByRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authRequest;
        // 兼容 get 和 post 操作
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        // 根据请求路径区分 id 字段的含义
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            String requestUri = request.getRequestURI();
            String partUri = requestUri.replace(contextPath + "/", "");
            String moduleName = StrUtil.subBefore(partUri, "/", false);
            switch (moduleName) {
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                default:
            }
        }
        return authRequest;
    }

    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true; // 对象本身为空
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }

}
