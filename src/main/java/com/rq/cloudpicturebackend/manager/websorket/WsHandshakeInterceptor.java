package com.rq.cloudpicturebackend.manager.websorket;


import cn.hutool.core.util.StrUtil;
import com.rq.cloudpicturebackend.enums.SpaceTypeEnum;
import com.rq.cloudpicturebackend.manager.auth.SpaceUserAuthManager;
import com.rq.cloudpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.rq.cloudpicturebackend.model.entity.Picture;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.PictureService;
import com.rq.cloudpicturebackend.service.SpaceService;
import com.rq.cloudpicturebackend.service.UserService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            UserLoginVo loginUser = userService.getLoginUser(servletRequest);
            //用户未登录，拒绝握手
            if (loginUser == null) {
                return false;
            }
            //从请求中获取照片信息
            String pictureId = servletRequest.getParameter("pictureId");
            if (StrUtil.isEmpty(pictureId)) {
                return false;
            }
            Picture picture = pictureService.getById(pictureId);
            //照片不存在，拒绝握手
            if (picture == null) {
                return false;
            }
            //校验空间ID是否存在
            if (picture.getSpaceId() == null) {
                return false;
            }
            //校验空间是否存在
            Space space = spaceService.getById(picture.getSpaceId());
            if (space == null) {
                return false;
            }
            //校验空间类型是否是团队空间
            if (SpaceTypeEnum.TEAM.getValue() != space.getSpaceType()) {
                return false;
            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            //用户没有编辑权限，拒绝握手
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                return false;
            }
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", picture.getId());
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler
            wsHandler, Exception exception) {

    }
}
