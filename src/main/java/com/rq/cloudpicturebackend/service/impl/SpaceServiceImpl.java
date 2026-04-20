package com.rq.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.cloudpicturebackend.common.DeletedRequest;
import com.rq.cloudpicturebackend.enums.SpaceLevelEnum;
import com.rq.cloudpicturebackend.enums.SpaceRoleEnum;
import com.rq.cloudpicturebackend.enums.SpaceTypeEnum;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.manager.auth.SpaceUserAuthManager;
import com.rq.cloudpicturebackend.manager.auth.StpKit;
import com.rq.cloudpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.rq.cloudpicturebackend.manager.sharding.DynamicShardingManager;
import com.rq.cloudpicturebackend.mapper.SpaceMapper;
import com.rq.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceEditRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.rq.cloudpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.entity.SpaceUser;
import com.rq.cloudpicturebackend.model.entity.User;
import com.rq.cloudpicturebackend.model.vo.SpaceVo;
import com.rq.cloudpicturebackend.model.vo.UserInfoVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.SpaceService;
import com.rq.cloudpicturebackend.service.SpaceUserService;
import com.rq.cloudpicturebackend.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    @Lazy
    private SpaceUserService spaceUserService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    @Lazy
    private DynamicShardingManager shardingManager;

    @Resource
    @Lazy
    private SpaceUserAuthManager spaceUserAuthManager;

    @Override
    public Boolean addSpace(SpaceAddRequest spaceAddRequest, UserLoginVo loginUser) {
        //校验用户是否登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //校验参数是否为空
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAM_ERROR);
        //校验空间名称不能为空
        ThrowUtils.throwIf(spaceAddRequest.getSpaceName() == null, ErrorCode.PARAM_ERROR);
        //校验空间级别
        Integer spaceLevel = spaceAddRequest.getSpaceLevel();
        ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAM_ERROR);
        //校验空间级别是否合法
        SpaceLevelEnum enumByCode = SpaceLevelEnum.getEnumByCode(spaceLevel);
        ThrowUtils.throwIf(enumByCode == null, ErrorCode.PARAM_ERROR, "空间级别错误");
        //如果用户不是管理员，并且空间级别不是普通版
        ThrowUtils.throwIf(!userService.isAdmin(loginUser) && !Objects.equals(SpaceLevelEnum.REGULAR.getValue(), enumByCode.getValue()), ErrorCode.NOT_AUTH_ERROR, "您没有权限创建此级别的空间");
        Integer spaceType = spaceAddRequest.getSpaceType();
        if (spaceType != null) {
            //校验空间类型是否合法
            SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
            ThrowUtils.throwIf(spaceTypeEnum == null, ErrorCode.PARAM_ERROR, "空间类型错误");
        } else {
            spaceAddRequest.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        fillSpaceParam(space, loginUser, enumByCode);
        String lockKey = "space:add:lock:" + loginUser.getId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁：等待时间10秒，锁超时时间30秒，启用看门狗
            boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (isLocked) {
                try {
                    //检验每个人每个类型只能创建一个空间
                    boolean exists = this.lambdaQuery().eq(Space::getUserId, loginUser.getId())
                            .eq(Space::getSpaceType, spaceAddRequest.getSpaceType()).exists();
                    ThrowUtils.throwIf(exists, ErrorCode.PARAM_ERROR, "每个人每个类型只能创建一个空间");
                    space.setUserId(loginUser.getId());
                    transactionTemplate.execute(status -> {
                        //创建空间
                        boolean result = this.save(space);
                        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "创建空间失败");
                        //如果是团队空间，默认将创建人加入空间用户表中
                        if (Objects.equals(SpaceTypeEnum.TEAM.getValue(), spaceType)) {
                            SpaceUser spaceUser = new SpaceUser();
                            spaceUser.setSpaceId(space.getId());
                            spaceUser.setUserId(loginUser.getId());
                            spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                            boolean userResult = spaceUserService.save(spaceUser);
                            ThrowUtils.throwIf(!userResult, ErrorCode.SYSTEM_ERROR, "创建空间用户失败");
                        }
                        if (SpaceLevelEnum.FLAGSHIP.getValue().equals(spaceLevel)) {
                            //如果是旗舰版需要分表存储
                            shardingManager.createSpacePictureTable(space);
                        }
                        return true;
                    });
                } finally {
                    lock.unlock();
                }
            } else {
                ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR, "系统繁忙，请重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }

    @Override
    public Boolean deleteSpace(DeletedRequest deletedRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(deletedRequest == null, ErrorCode.PARAM_ERROR);
        long id = deletedRequest.getId();
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAM_ERROR);
        Space space = this.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.PARAM_ERROR, "空间不存在");
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR, "您没有权限删除此空间");
        //删除空间
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "删除空间失败");
        return true;
    }

    @Override
    public Boolean updateSpace(SpaceUpdateRequest spaceUpdateRequest, UserLoginVo loginUser) {
        //检查参数是否合规
        ThrowUtils.throwIf(spaceUpdateRequest == null, ErrorCode.PARAM_ERROR);
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);
        checkAndFillParam(loginUser, space);
        boolean result = this.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新空间失败");
        return true;
    }

    @Override
    public Boolean editSpace(SpaceEditRequest spaceEditRequest, UserLoginVo loginUser) {
        //检查参数是否合规
        ThrowUtils.throwIf(spaceEditRequest == null, ErrorCode.PARAM_ERROR);
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);
        checkAndFillParam(loginUser, space);
        space.setEditTime(new Date());
        boolean result = this.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "编辑空间失败");
        return true;
    }

    @Override
    public Page<Space> listPage(SpaceQueryRequest spaceQueryRequest) {
        //查询参数不能为空
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAM_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        return this.page(new Page<>(current, pageSize), getSpaceQueryWrapper(spaceQueryRequest));
    }

    @Override
    public Page<SpaceVo> listPageVo(SpaceQueryRequest spaceQueryRequest, UserLoginVo loginUser) {
        //查询参数不能为空
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAM_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        Page<Space> page = this.page(new Page<>(current, pageSize), getSpaceQueryWrapper(spaceQueryRequest));
        Page<SpaceVo> spaceVoPage = new Page<>();
        BeanUtil.copyProperties(page, spaceVoPage);
        List<Space> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            Map<Long, UserInfoVo> userMap = new HashMap<>();
            List<Long> userIds = records.stream().filter(ObjUtil::isNotEmpty).map(Space::getUserId).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(userIds)) {
                List<User> userList = userService.listByIds(userIds);
                userMap = CollUtil.isNotEmpty(userList) ? userList.stream().map(user -> {
                    UserInfoVo userInfoVo = new UserInfoVo();
                    BeanUtil.copyProperties(user, userInfoVo);
                    return userInfoVo;
                }).collect(Collectors.toMap(UserInfoVo::getId, userInfo -> userInfo)) : new HashMap<>();

            }
            Map<Long, UserInfoVo> finalUserMap = userMap;
            List<SpaceVo> spaceVoList = records.stream().map(space -> {
                SpaceVo spaceVo = new SpaceVo();
                BeanUtil.copyProperties(space, spaceVo);
                UserInfoVo userInfoVo = finalUserMap.get(space.getUserId());
                if (userInfoVo != null) {
                    spaceVo.setUserVo(userInfoVo);
                }
                return spaceVo;
            }).collect(Collectors.toList());
            spaceVoPage.setRecords(spaceVoList);
        }
        return spaceVoPage;
    }

    @Override
    public SpaceVo getSpaceVoById(Long id, UserLoginVo loginUser) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Space space = this.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.SYSTEM_ERROR, "空间不存在");
        //校验是否是有权查看
        //ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()) && !userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR);
        SpaceVo spaceVo = new SpaceVo();
        BeanUtil.copyProperties(space, spaceVo);
        boolean hasPermission = StpKit.SPACE.hasPermission(loginUser.getId(), SpaceUserPermissionConstant.PICTURE_VIEW);
        ThrowUtils.throwIf(!hasPermission, ErrorCode.NOT_AUTH_ERROR, "没有权限");
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        spaceVo.setPermissionList(permissionList);
        User user = userService.getById(space.getUserId());
        if (user != null) {
            UserInfoVo userInfoVo = new UserInfoVo();
            BeanUtil.copyProperties(user, userInfoVo);
            spaceVo.setUserVo(userInfoVo);
        }
        return spaceVo;

    }

    @Override
    public void calculateSpaceUsage(Long spaceId) {
        spaceMapper.calculateSpaceUsage(spaceId);
    }

    private QueryWrapper<Space> getSpaceQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        Long id = spaceQueryRequest.getId();
        if (id != null && id > 0) {
            queryWrapper.eq("id", id);
        }
        String spaceName = spaceQueryRequest.getSpaceName();
        if (StrUtil.isNotBlank(spaceName)) {
            queryWrapper.like("spaceName", spaceName);
        }
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        if (spaceLevel != null) {
            queryWrapper.eq("spaceLevel", spaceLevel);
        }
        Long maxSize = spaceQueryRequest.getMaxSize();
        if (maxSize != null) {
            queryWrapper.eq("maxSize", maxSize);
        }
        Long maxCount = spaceQueryRequest.getMaxCount();
        if (maxCount != null) {
            queryWrapper.eq("maxCount", maxCount);
        }
        Long totalSize = spaceQueryRequest.getTotalSize();
        if (totalSize != null) {
            queryWrapper.eq("totalSize", totalSize);
        }
        Long totalCount = spaceQueryRequest.getTotalCount();
        if (totalCount != null) {
            queryWrapper.eq("totalCount", totalCount);
        }
        Long userId = spaceQueryRequest.getUserId();
        if (userId != null && userId > 0) {
            queryWrapper.eq("userId", userId);
        }
        Integer spaceType = spaceQueryRequest.getSpaceType();
        if (spaceType != null) {
            queryWrapper.eq("spaceType", spaceType);
        }
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            if (StrUtil.equals(sortOrder, "asc")) {
                queryWrapper.orderByAsc(sortField);
            } else {
                queryWrapper.orderByDesc(sortField);
            }
        } else {
            queryWrapper.orderByDesc("createTime");
        }
        return queryWrapper;
    }


    /**
     * 校验并填充数据
     *
     * @param loginUser 登录用户
     * @param space     空间
     */
    private void checkAndFillParam(UserLoginVo loginUser, Space space) {
        //检查是否登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //检查空间ID是否合规
        ThrowUtils.throwIf(space.getId() == null || space.getId() <= 0, ErrorCode.PARAM_ERROR);
        //检查空间名称是否合规
        ThrowUtils.throwIf(space.getSpaceName() == null, ErrorCode.PARAM_ERROR);
        //检查空间级别是否合规
        ThrowUtils.throwIf(space.getSpaceLevel() == null, ErrorCode.PARAM_ERROR);
        //检查空间级别是否合规
        SpaceLevelEnum enumByCode = SpaceLevelEnum.getEnumByCode(space.getSpaceLevel());
        ThrowUtils.throwIf(enumByCode == null, ErrorCode.PARAM_ERROR, "空间级别错误");
        //检查空间是否存在
        Space oldSpace = this.getById(space.getId());
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.PARAM_ERROR, "空间不存在");
        //检查空间是否属于当前用户
        ThrowUtils.throwIf(!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR, "您没有权限编辑此空间");
        fillSpaceParam(space, loginUser, enumByCode);
    }

    /**
     * 填充空间参数
     *
     * @param space      空间
     * @param loginUser  登录用户
     * @param enumByCode 空间级别枚举
     */
    private void fillSpaceParam(Space space, UserLoginVo loginUser, SpaceLevelEnum enumByCode) {
        //获取空间数量
        Long count = enumByCode.getCount();
        //获取空间大小
        Long size = enumByCode.getSize();
        if (userService.isAdmin(loginUser)) {
            //如果用户是管理员，则不限制空间数量和大小
            Long maxSize = space.getMaxSize();
            if (maxSize != null) {
                ThrowUtils.throwIf(maxSize <= 0, ErrorCode.PARAM_ERROR);
                size = maxSize;
            }
            Long maxCount = space.getMaxCount();
            if (maxCount != null) {
                ThrowUtils.throwIf(maxCount <= 0, ErrorCode.PARAM_ERROR);
                count = maxCount;
            }
        }
        space.setMaxCount(count);
        space.setMaxSize(size);
    }
}




