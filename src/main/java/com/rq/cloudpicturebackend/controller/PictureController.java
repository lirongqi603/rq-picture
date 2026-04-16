package com.rq.cloudpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.cloudpicturebackend.annotation.AuthCheck;
import com.rq.cloudpicturebackend.api.aliyun.model.CreateTaskRequest;
import com.rq.cloudpicturebackend.api.aliyun.model.CreateTaskResponse;
import com.rq.cloudpicturebackend.api.aliyun.model.QueryTaskResponse;
import com.rq.cloudpicturebackend.common.BaseResponse;
import com.rq.cloudpicturebackend.common.DeletedRequest;
import com.rq.cloudpicturebackend.common.ResultUtils;
import com.rq.cloudpicturebackend.constant.UserConstant;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.model.dto.picture.BatchUploadPictureRequest;
import com.rq.cloudpicturebackend.model.dto.picture.UploadPictureRequest;
import com.rq.cloudpicturebackend.model.dto.picture.*;
import com.rq.cloudpicturebackend.model.entity.Picture;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.vo.PictureTagCategory;
import com.rq.cloudpicturebackend.model.vo.PictureVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.PictureService;
import com.rq.cloudpicturebackend.service.SpaceService;
import com.rq.cloudpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    /**
     * 图片上传接口
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVo> uploadPicture(@RequestPart(value = "file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        Boolean ignoreSize = false;
        PictureVo pictureVo = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser, ignoreSize);
        return ResultUtils.success(pictureVo);
    }


    /**
     * 修改图片(管理员端)
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        boolean result = pictureService.updatePicture(pictureUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 图片上传接口(用户端)
     */
    @PostMapping("/user/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,
                                             HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        boolean result = pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 图片删除接口
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeletedRequest deletedRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(deletedRequest == null || deletedRequest.getId() <= 0, ErrorCode.PARAM_ERROR);
        UserLoginVo loginUser = userService.getLoginUser(request);
        boolean result = pictureService.deletePicture(deletedRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 图片查询接口(管理员端)
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPagePictures(@RequestBody PictureQueryRequest pictureQueryRequest) {
        Page<Picture> picturePage = pictureService.listPagePictures(pictureQueryRequest);
        return ResultUtils.success(picturePage);
    }

    /**
     * 图片查询接口(用户端)
     */
    @PostMapping("/list/pageVo")
    public BaseResponse<Page<PictureVo>> listPagePictureVos(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                            HttpServletRequest request) {
        Page<PictureVo> pictureVoPage = pictureService.listPagePictureVos(pictureQueryRequest, request);
        return ResultUtils.success(pictureVoPage);
    }

    /**
     * 图片查询接口(用户端)
     */
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVo>> searchPictureListByColor(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                  HttpServletRequest request) {
        List<PictureVo> pictureVoList = pictureService.searchPictureListByColor(pictureQueryRequest, request);
        return ResultUtils.success(pictureVoList);
    }

    /**
     * 根据ID查询图片信息(管理员端)
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAM_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    /**
     * 根据ID查询图片信息(用户端)
     */
    @GetMapping("/getVo")
    public BaseResponse<PictureVo> getPictureVoById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAM_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            UserLoginVo loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NOT_AUTH_ERROR, "没有权限");
        }
        return ResultUtils.success(pictureService.getPictureVo(picture));
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 图片审核
     */
    @PostMapping("/reviewPicture")
    public BaseResponse<Boolean> reviewPicture(@RequestBody PictureReviewRequest pictureQueryRequest,
                                               HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        pictureService.reviewPicture(pictureQueryRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 通过url获取图片信息并上传图片返回图片信息
     */
    @PostMapping("/uploadByUrl")
    public BaseResponse<PictureVo> uploadPictureByUrl(@RequestBody UploadPictureRequest uploadPictureRequest, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(uploadPictureRequest == null || uploadPictureRequest.getUrl() == null, ErrorCode.PARAM_ERROR);
        PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
        BeanUtil.copyProperties(uploadPictureRequest, pictureUploadRequest);
        Boolean ignoreSize = false;
        PictureVo pictureVo = pictureService.uploadPicture(uploadPictureRequest.getUrl(), pictureUploadRequest, loginUser, ignoreSize);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 批量获取图片
     */
    @PostMapping("/batchUploadPicture")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> batchUploadPicture(@RequestBody BatchUploadPictureRequest batchUploadPictureRequest, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        Integer cnt = pictureService.batchUploadPicture(batchUploadPictureRequest, loginUser);
        return ResultUtils.success(cnt);
    }

    /**
     * 批量修改图片
     */
    @PostMapping("/batchUpdatePicture")
    public BaseResponse<Integer> batchUpdatePicture(@RequestBody BatchUpdatePictureRequest batchUpdatePictureRequest, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        Integer cnt = pictureService.batchUpdatePicture(batchUpdatePictureRequest, loginUser);
        return ResultUtils.success(cnt);
    }

    /**
     * 创建AI图像扩展任务
     */
    @PostMapping("/createAiTask")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<CreateTaskResponse> createAiTask(@RequestBody CreateTaskRequest createTaskRequest, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        CreateTaskResponse createTaskResponse = pictureService.createAiTask(createTaskRequest, loginUser);
        return ResultUtils.success(createTaskResponse);
    }

    /**
     * 获取AI图像扩展任务进度
     */
    @GetMapping("/getAiTaskProgress")
    public BaseResponse<QueryTaskResponse> getAiTaskProgress(@RequestParam("taskId") String taskId, HttpServletRequest request) {
        UserLoginVo loginUser = userService.getLoginUser(request);
        QueryTaskResponse queryTaskResponse = pictureService.getAiTaskProgress(taskId, loginUser);
        return ResultUtils.success(queryTaskResponse);
    }

}
