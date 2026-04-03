package com.rq.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.cloudpicturebackend.constant.UserConstant;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.manager.FileManager;
import com.rq.cloudpicturebackend.mapper.PictureMapper;
import com.rq.cloudpicturebackend.model.dto.file.UploadPictureResult;
import com.rq.cloudpicturebackend.model.dto.picture.PictureEditRequest;
import com.rq.cloudpicturebackend.model.dto.picture.PictureQueryRequest;
import com.rq.cloudpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.rq.cloudpicturebackend.model.dto.picture.PictureUploadRequest;
import com.rq.cloudpicturebackend.model.entity.Picture;
import com.rq.cloudpicturebackend.model.entity.User;
import com.rq.cloudpicturebackend.model.vo.PictureVo;
import com.rq.cloudpicturebackend.model.vo.UserInfoVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.PictureService;
import com.rq.cloudpicturebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 图片服务实现类
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private FileManager fileManager;
    @Resource
    private UserService userService;

    @Override
    public PictureVo uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, UserLoginVo loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_AUTH_ERROR);
        //判断是新增还是删除
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        if (pictureId != null) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        PictureVo pictureVo = PictureVo.objToVo(picture);
        UserInfoVo userInfo = userService.getUserInfoById(picture.getUserId());
        pictureVo.setUser(userInfo);
        return pictureVo;
    }

    @Override
    public boolean updatePicture(PictureUpdateRequest pictureUpdateRequest) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        validPictureParam(picture);
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片更新失败");
        return true;
    }

    @Override
    public boolean editPicture(PictureEditRequest pictureEditRequest, UserLoginVo loginUser) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        validPictureParam(picture);
        //校验权限
        ThrowUtils.throwIf(!picture.getUserId().equals(loginUser.getId()) && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()), ErrorCode.NOT_AUTH_ERROR, "无权限编辑图片");
        picture.setEditTime(new Date());
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片编辑失败");
        return false;
    }

    private void validPictureParam(Picture picture) {
        //请求参数校验
        ThrowUtils.throwIf(picture == null || picture.getId() == null, ErrorCode.PARAM_ERROR);
        //图片名称不能为空
        ThrowUtils.throwIf(StringUtils.isBlank(picture.getName()), ErrorCode.PARAM_ERROR, "图片名称不能为空");
        //图片长度不能超过50
        ThrowUtils.throwIf(StrUtil.length(picture.getName()) > 50, ErrorCode.PARAM_ERROR, "图片名称长度不能超过50");
        //简介长度不能超过200
        ThrowUtils.throwIf(StrUtil.length(picture.getIntroduction()) > 200, ErrorCode.PARAM_ERROR, "图片简介长度不能超过200");
        picture = this.getById(picture.getId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
    }

    @Override
    public boolean deletePicture(Long id, UserLoginVo loginUser) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAM_ERROR);
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(!picture.getUserId().equals(loginUser.getId()) && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()), ErrorCode.NOT_AUTH_ERROR, "无权限删除图片");
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除图片失败");
        return true;
    }

    @Override
    public Page<Picture> listPagePictures(PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAM_ERROR);
        getPictureQueryWrapper(pictureQueryRequest);
        return this.page(new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize()),
                getPictureQueryWrapper(pictureQueryRequest));
    }


    @Override
    public Page<PictureVo> listPagePictureVos(PictureQueryRequest pictureQueryRequest, UserLoginVo loginUser) {
        //请求参数为空
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAM_ERROR);
        int pageSize = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 100, ErrorCode.PARAM_ERROR, "每页记录数不能超过100");
        Page<Picture> pageList = this.page(new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize()),
                getPictureQueryWrapper(pictureQueryRequest));
        return getPictureVoPage(pageList);
    }

    @Override
    public PictureVo getPictureVo(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAM_ERROR);
        PictureVo pictureVo = BeanUtil.copyProperties(picture, PictureVo.class);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            UserInfoVo userInfo = userService.getUserInfoById(userId);
            if (userInfo != null) {
                pictureVo.setUser(userInfo);
            }
        }
        pictureVo.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVo;
    }

    /**
     * 获取图片查询条件
     *
     * @param pictureQueryRequest 图片查询请求
     * @description 获取图片查询条件
     */
    private LambdaQueryWrapper<Picture> getPictureQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        LambdaQueryWrapper<Picture> queryWrapper = new LambdaQueryWrapper<>();
        if (pictureQueryRequest != null) {
            //根据关键字搜索
            String searchText = pictureQueryRequest.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(wrapper -> wrapper.like(Picture::getName, searchText).or().like(Picture::getIntroduction, searchText));
            }
            //根据名称模糊查询
            String name = pictureQueryRequest.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like(Picture::getName, name);
            }
            //根据简介模糊查询
            String introduction = pictureQueryRequest.getIntroduction();
            if (StringUtils.isNotBlank(introduction)) {
                queryWrapper.like(Picture::getIntroduction, introduction);
            }
            //根据标签模糊查询
            List<String> tags = pictureQueryRequest.getTags();
            if (CollUtil.isNotEmpty(tags)) {
                queryWrapper.like(Picture::getTags, JSONUtil.toJsonStr(tags));
            }
            //根据用户ID查询
            Long userId = pictureQueryRequest.getUserId();
            if (userId != null) {
                queryWrapper.eq(Picture::getUserId, userId);
            }
            //根据图片体积查询
            Long picSize = pictureQueryRequest.getPicSize();
            if (picSize != null) {
                queryWrapper.eq(Picture::getPicSize, picSize);
            }
            //根据图片宽度查询
            Integer picWidth = pictureQueryRequest.getPicWidth();
            if (picWidth != null) {
                queryWrapper.eq(Picture::getPicWidth, picWidth);
            }
            //根据图片高度查询
            Integer picHeight = pictureQueryRequest.getPicHeight();
            if (picHeight != null) {
                queryWrapper.eq(Picture::getPicHeight, picHeight);
            }
            //根据图片格式查询
            String picFormat = pictureQueryRequest.getPicFormat();
            if (StringUtils.isNotBlank(picFormat)) {
                queryWrapper.eq(Picture::getPicFormat, picFormat);
            }
        }
        return queryWrapper;
    }

    /**
     * 获取图片分页视图对象
     *
     * @param pageList 图片分页列表
     * @return page对象
     */
    private Page<PictureVo> getPictureVoPage(Page<Picture> pageList) {
        Page<PictureVo> pictureVoPage = new Page<>(pageList.getCurrent(), pageList.getSize(), pageList.getTotal());
        List<Picture> pictureList = pageList.getRecords();
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVoPage;
        }
        Set<Long> userId = pictureList.stream().map(Picture::getUserId).filter(Objects::nonNull).filter(id -> id > 0).collect(Collectors.toSet());
        Map<Long, UserInfoVo> userInfoMap = new HashMap<>();
        if (userId.size() > 0) {
            List<User> users = userService.listByIds(userId);
            if (users != null && users.size() > 0) {
                List<UserInfoVo> userInfoVos = BeanUtil.copyToList(users, UserInfoVo.class);
                userInfoMap = userInfoVos.stream().collect(Collectors.toMap(UserInfoVo::getId, Function.identity()));
            }
        }
        Map<Long, UserInfoVo> finalUserInfoMap = userInfoMap;
        List<PictureVo> pictureVos = pictureList.stream().map(picture -> {
            PictureVo pictureVo = BeanUtil.copyProperties(picture, PictureVo.class);
            pictureVo.setTags(JSONUtil.toList(picture.getTags(), String.class));
            UserInfoVo userInfoVo = finalUserInfoMap.get(picture.getUserId());
            if (userInfoVo != null) {
                pictureVo.setUser(userInfoVo);
            }
            return pictureVo;
        }).collect(Collectors.toList());
        pictureVoPage.setRecords(pictureVos);
        return pictureVoPage;
    }
}





