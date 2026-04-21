package com.rq.cloudpicturebackend.manager.websorket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rq.cloudpicturebackend.manager.websorket.disruptor.PictureEditEventProducer;
import com.rq.cloudpicturebackend.manager.websorket.model.PictureEditActionEnum;
import com.rq.cloudpicturebackend.manager.websorket.model.PictureEditMessageTypeEnum;
import com.rq.cloudpicturebackend.manager.websorket.model.PictureEditRequestMessage;
import com.rq.cloudpicturebackend.manager.websorket.model.PictureEditResponseMessage;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;

    // 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
    private final Map<Long, UserLoginVo> pictureEditingUsers = new ConcurrentHashMap<>();

    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws Exception {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
            objectMapper.registerModule(module);
            // 序列化为 JSON 字符串
            pictureEditResponseMessage.setUser(pictureEditingUsers.get(pictureId));
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessionSet) {
                // 排除掉的 session 不发送
                if (excludeSession != null && excludeSession.equals(session)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    // 全部广播
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        UserLoginVo loginUser = (UserLoginVo) session.getAttributes().get("user");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);

        //构建响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
        pictureEditResponseMessage.setMessage("用户 " + loginUser.getUserName() + " 进入编辑模式");
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 将消息解析为 PictureEditMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.valueOf(type);

        // 从 Session 属性中获取公共参数
        Map<String, Object> attributes = session.getAttributes();
        UserLoginVo loginUser = (UserLoginVo) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");
        // 调用对应的消息处理方法
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, loginUser, pictureId);
    }

    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, UserLoginVo loginUser, Long pictureId) throws Exception {
        // 没有用户正在编辑该图片，才能进入编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            // 设置当前用户为编辑用户
            pictureEditingUsers.put(pictureId, loginUser);
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("%s开始编辑图片", loginUser.getUserName());
            pictureEditResponseMessage.setMessage(message);
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, UserLoginVo loginUser, Long pictureId) throws Exception {
        UserLoginVo editUser = pictureEditingUsers.get(pictureId);
        if (editUser == null) {
            return;
        }
        Long editingUserId = editUser.getId();
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (actionEnum == null) {
            return;
        }
        // 确认是当前编辑者
        if (editingUserId != null && editingUserId.equals(loginUser.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("%s执行%s", loginUser.getUserName(), actionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            // 广播给除了当前客户端之外的其他用户，否则会造成重复编辑
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }

    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, UserLoginVo loginUser, Long pictureId) throws Exception {
        UserLoginVo editUser = pictureEditingUsers.get(pictureId);
        if (editUser == null) {
            return;
        }
        Long editingUserId = editUser.getId();
        if (editingUserId != null && editingUserId.equals(loginUser.getId())) {
            // 移除当前用户的编辑状态
            pictureEditingUsers.remove(pictureId);
            // 构造响应，发送退出编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("%s退出编辑图片", loginUser.getUserName());
            pictureEditResponseMessage.setMessage(message);
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        UserLoginVo loginUser = (UserLoginVo) attributes.get("user");
        // 移除当前用户的编辑状态
        handleExitEditMessage(null, session, loginUser, pictureId);

        // 删除会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }

        // 响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("%s离开编辑", loginUser.getUserName());
        pictureEditResponseMessage.setMessage(message);
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }
}
