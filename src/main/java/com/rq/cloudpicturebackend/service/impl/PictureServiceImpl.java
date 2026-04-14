package com.rq.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.cloudpicturebackend.constant.UserConstant;
import com.rq.cloudpicturebackend.enums.ReviewStatusEnum;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.manager.CosManager;
import com.rq.cloudpicturebackend.manager.upload.FileUploadPictureImpl;
import com.rq.cloudpicturebackend.manager.upload.UploadPictureTemplate;
import com.rq.cloudpicturebackend.manager.upload.UrlUploadPictureImpl;
import com.rq.cloudpicturebackend.mapper.PictureMapper;
import com.rq.cloudpicturebackend.model.dto.file.UploadPictureResult;
import com.rq.cloudpicturebackend.model.dto.picture.*;
import com.rq.cloudpicturebackend.model.entity.Picture;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.entity.User;
import com.rq.cloudpicturebackend.model.vo.PictureVo;
import com.rq.cloudpicturebackend.model.vo.UserInfoVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.PictureService;
import com.rq.cloudpicturebackend.service.SpaceService;
import com.rq.cloudpicturebackend.service.UserService;
import com.rq.cloudpicturebackend.utill.BingImageByOffset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 图片服务实现类
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private FileUploadPictureImpl fileUploadPicture;
    @Resource
    private UrlUploadPictureImpl urlUploadPicture;
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private CosManager cosManager;

    @Override
    public PictureVo uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, UserLoginVo loginUser, Boolean ignoreSize) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_AUTH_ERROR);
        //判断是新增还是修改
        Long pictureId = null;
        Long spaceId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
            spaceId = pictureUploadRequest.getSpaceId();
        }
        if (pictureId != null) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!Objects.equals(space.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR, "无权限操作");
            Long maxCount = space.getMaxCount();
            Long maxSize = space.getMaxSize();
            Long totalCount = space.getTotalCount();
            Long totalSize = space.getTotalSize();
            ThrowUtils.throwIf(totalCount != null && totalCount >= maxCount, ErrorCode.OPERATION_ERROR, "空间已满");
            ThrowUtils.throwIf(totalSize != null && totalSize >= maxSize, ErrorCode.OPERATION_ERROR, "空间已满");
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        UploadPictureTemplate uploadPictureTemplate = fileUploadPicture;
        if (inputSource instanceof String) {
            uploadPictureTemplate = urlUploadPicture;
        }
        UploadPictureResult uploadPictureResult = uploadPictureTemplate.uploadPicture(inputSource, uploadPathPrefix, ignoreSize);
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setSpaceId(spaceId);
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null) {
            String name = pictureUploadRequest.getName();
            if (StrUtil.isNotBlank(name)) {
                picName = name;
            }
            String category = pictureUploadRequest.getCategory();
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            List<String> tagList = pictureUploadRequest.getTagList();
            if (CollUtil.isNotEmpty(tagList)) {
                picture.setTags(JSONUtil.toJsonStr(tagList));
            }
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        fullReviewInfo(picture, loginUser);
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        Long finalSpaceId = spaceId;
        //事务
        try {
            // 2. 执行数据库事务
            transactionTemplate.execute(status -> {
                boolean result = this.saveOrUpdate(picture);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
                if (finalSpaceId != null) {
                    spaceService.calculateSpaceUsage(finalSpaceId);
                }
                return true;
            });
        } catch (Exception e) {
            delCosPicture(picture);
            throw e;
        }
        PictureVo pictureVo = PictureVo.objToVo(picture);
        UserInfoVo userInfo = userService.getUserInfoById(picture.getUserId());
        pictureVo.setUser(userInfo);
        return pictureVo;
    }

    /**
     * 删除对象存储中的图片
     *
     * @param picture
     */
    private void delCosPicture(Picture picture) {
        List<String> urls = new ArrayList<>();
        if (StrUtil.isNotBlank(picture.getUrl())) {
            urls.add(picture.getUrl());
        }
        if (StrUtil.isNotBlank(picture.getThumbnailUrl())) {
            urls.add(picture.getThumbnailUrl());
        }
        if (CollUtil.isNotEmpty(urls)) {
            cosManager.delBatchObject(urls);
        }
    }

    @Override
    public boolean updatePicture(PictureUpdateRequest pictureUpdateRequest, UserLoginVo loginUser) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        validPictureParam(picture);
        fullReviewInfo(picture, loginUser);
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片更新失败");
        return true;
    }

    @Override
    public boolean editPicture(PictureEditRequest pictureEditRequest, UserLoginVo loginUser) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        Picture oldPicture = validPictureParam(picture);
        //校验权限
        ThrowUtils.throwIf(!oldPicture.getUserId().equals(loginUser.getId()) && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()), ErrorCode.NOT_AUTH_ERROR, "无权限编辑图片");
        picture.setEditTime(new Date());
        fullReviewInfo(picture, loginUser);
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片编辑失败");
        return true;
    }

    /**
     * 校验参数
     *
     * @param picture 图片
     * @return 图片
     */
    private Picture validPictureParam(Picture picture) {
        //请求参数校验
        ThrowUtils.throwIf(picture == null || picture.getId() == null, ErrorCode.PARAM_ERROR);
        //图片名称不能为空
        ThrowUtils.throwIf(StringUtils.isBlank(picture.getName()), ErrorCode.PARAM_ERROR, "图片名称不能为空");
        //图片长度不能超过50
        ThrowUtils.throwIf(StrUtil.length(picture.getName()) > 50, ErrorCode.PARAM_ERROR, "图片名称长度不能超过50");
        //简介长度不能超过200
        ThrowUtils.throwIf(StrUtil.length(picture.getIntroduction()) > 200, ErrorCode.PARAM_ERROR, "图片简介长度不能超过200");
        Picture oldPicture = this.getById(picture.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return oldPicture;
    }

    /**
     * 补全审核信息
     *
     * @param picture   图片信息
     * @param loginUser 登录用户
     */
    public void fullReviewInfo(Picture picture, UserLoginVo loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewerId(loginUser.getId());
            picture.setReviewStatus(ReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(ReviewStatusEnum.PENDING.getValue());
        }
    }

    @Override
    public boolean deletePicture(Long id, UserLoginVo loginUser) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAM_ERROR);
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(!picture.getUserId().equals(loginUser.getId()) && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()), ErrorCode.NOT_AUTH_ERROR, "无权限删除图片");
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除图片失败");
        Long spaceId = picture.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            spaceService.calculateSpaceUsage(spaceId);
        }
        delCosPicture(picture);
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
    public Page<PictureVo> listPagePictureVos(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        //请求参数为空
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAM_ERROR);
        Boolean isPublic = pictureQueryRequest.getIsPublic();
        if (!isPublic) {
            Long spaceId = pictureQueryRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAM_ERROR, "空间不能为空");
            UserLoginVo loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NOT_AUTH_ERROR, "无权限访问该空间");
        } else {
            int pageSize = pictureQueryRequest.getPageSize();
            ThrowUtils.throwIf(pageSize > 100, ErrorCode.PARAM_ERROR, "每页记录数不能超过100");
            pictureQueryRequest.setReviewStatus(ReviewStatusEnum.PASS.getValue());
        }
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

    @Override
    public void reviewPicture(PictureReviewRequest pictureQueryRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR);
        Long id = pictureQueryRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAM_ERROR);
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        ThrowUtils.throwIf(ReviewStatusEnum.getEnumByCode(reviewStatus) == null, ErrorCode.PARAM_ERROR);
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureQueryRequest, picture);
        picture.setReviewerId(loginUser.getId());
        picture.setReviewTime(new Date());
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR);
    }

    @Override
    public Integer batchUploadPicture(BatchUploadPictureRequest batchUploadPictureRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(batchUploadPictureRequest == null, ErrorCode.PARAM_ERROR);
        String searchText = batchUploadPictureRequest.getSearchText();
        ThrowUtils.throwIf(StringUtils.isBlank(searchText), ErrorCode.PARAM_ERROR, "搜索内容不能为空");
        Integer searchPage = batchUploadPictureRequest.getSearchPage();
        ThrowUtils.throwIf(searchPage == null || searchPage <= 0, ErrorCode.PARAM_ERROR);
        Integer searchNum = batchUploadPictureRequest.getSearchNum();
        ThrowUtils.throwIf(searchNum == null || searchNum <= 0, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(searchNum > 30, ErrorCode.PARAM_ERROR, "搜索数量不能大于30");
        String namePrefix = batchUploadPictureRequest.getNamePrefix();
        if (StringUtils.isBlank(namePrefix)) {
            namePrefix = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);
        }
        int successNum = 0;
        try {
            List<String> imageUrls = BingImageByOffset.getImageUrlsByOffset(searchText, searchPage, searchNum);
            if (CollUtil.isEmpty(imageUrls)) {
                return successNum;
            }
            for (String imageUrl : imageUrls) {
                int idx = imageUrl.indexOf("?");
                if (idx > -1) {
                    imageUrl = imageUrl.substring(0, idx);
                }
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                BeanUtil.copyProperties(batchUploadPictureRequest, pictureUploadRequest);
                pictureUploadRequest.setUrl(imageUrl);
                pictureUploadRequest.setName(namePrefix + "_" + (successNum + 1));
                try {
                    PictureVo pictureVo = this.uploadPicture(imageUrl, pictureUploadRequest, loginUser, true);
                    if (pictureVo != null) {
                        successNum++;
                        log.info("图片上传成功，文件地址：{}，图片ID:{}", imageUrl, pictureVo.getId());
                    }
                    if (successNum >= searchNum) {
                        break;
                    }
                } catch (Exception e) {
                    log.error("图片上传失败，文件地址：{}", imageUrl);
                }
            }
        } catch (IOException e) {
            log.error("获取图片失败", e);
            ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR, "获取图片失败");
        }

        return successNum;
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
            //根据类型
            String category = pictureQueryRequest.getCategory();
            if (StringUtils.isNotBlank(category)) {
                queryWrapper.eq(Picture::getCategory, category);
            }
            //根据标签模糊查询
            List<String> tags = pictureQueryRequest.getTags();
            if (CollUtil.isNotEmpty(tags)) {
                //标签之间or关联，标签外and关联
                queryWrapper.and(wrapper -> {
                    for (String tag : tags) {
                        wrapper.or().like(Picture::getTags, tag);
                    }
                });
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
            //根据审核人查找
            Long reviewerId = pictureQueryRequest.getReviewerId();
            if (reviewerId != null) {
                queryWrapper.eq(Picture::getReviewerId, reviewerId);
            }
            //根据审核状态查找
            Integer reviewStatus = pictureQueryRequest.getReviewStatus();
            if (reviewStatus != null) {
                queryWrapper.eq(Picture::getReviewStatus, reviewStatus);
            }
            //根据审核信息查找
            String reviewMessage = pictureQueryRequest.getReviewMessage();
            if (StringUtils.isNotBlank(reviewMessage)) {
                queryWrapper.like(Picture::getReviewMessage, reviewMessage);
            }
            //进查询公共图库图片
            Boolean isPublic = pictureQueryRequest.getIsPublic();
            Long spaceId = pictureQueryRequest.getSpaceId();
            if (isPublic) {
                queryWrapper.isNull(Picture::getSpaceId);
            } else if (spaceId != null) {
                queryWrapper.eq(Picture::getSpaceId, spaceId);
            }
            String sortField = pictureQueryRequest.getSortField();
            String sortOrder = pictureQueryRequest.getSortOrder();
            if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortOrder)) {
                if ("asc".equals(sortOrder)) {
                    queryWrapper.orderByAsc(Picture::getCreateTime);
                } else
                    queryWrapper.orderByDesc(Picture::getCreateTime);
            } else {
                queryWrapper.orderByDesc(Picture::getCreateTime);
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





