package com.rq.cloudpicturebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.cloudpicturebackend.api.aliyun.model.CreateTaskRequest;
import com.rq.cloudpicturebackend.api.aliyun.model.CreateTaskResponse;
import com.rq.cloudpicturebackend.api.aliyun.model.QueryTaskResponse;
import com.rq.cloudpicturebackend.model.dto.picture.*;
import com.rq.cloudpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.cloudpicturebackend.model.vo.PictureVo;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 图片服务
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource          图片文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @param ignoreSize           是否忽略大小
     * @return 图片上传结果
     */
    PictureVo uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, UserLoginVo loginUser, Boolean ignoreSize);

    /**
     * 修改图片
     *
     * @param pictureUpdateRequest 图片修改请求
     * @param loginUser            用户信息
     * @return 修改结果
     */
    boolean updatePicture(PictureUpdateRequest pictureUpdateRequest, UserLoginVo loginUser);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest 图片编辑请求
     * @param loginUser          登录用户
     * @return 编辑结果
     */
    boolean editPicture(PictureEditRequest pictureEditRequest, UserLoginVo loginUser);

    /**
     * 删除图片
     *
     * @param id        图片ID
     * @param loginUser 登录用户
     * @return 删除结果
     */
    boolean deletePicture(Long id, UserLoginVo loginUser);

    /**
     * 分页查询图片
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 图片分页结果
     */
    Page<Picture> listPagePictures(PictureQueryRequest pictureQueryRequest);

    /**
     * 分页查询图片VO
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 图片VO分页结果
     */
    Page<PictureVo> listPagePictureVos(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    /**
     * 获取图片VO
     *
     * @param picture 图片实体
     * @return 图片VO
     */
    PictureVo getPictureVo(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureQueryRequest 请求
     * @param loginUser           用户
     */
    void reviewPicture(PictureReviewRequest pictureQueryRequest, UserLoginVo loginUser);

    /**
     * 批量获取图片
     *
     * @param batchUploadPictureRequest 请求
     * @param loginUser                 用户
     * @return 获取数量
     */
    Integer batchUploadPicture(BatchUploadPictureRequest batchUploadPictureRequest, UserLoginVo loginUser);

    /**
     * 根据颜色搜索图片
     *
     * @param pictureQueryRequest 请求
     * @param request             请求
     * @return 图片列表
     */
    List<PictureVo> searchPictureListByColor(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    /**
     * 批量修改图片
     *
     * @param batchUpdatePictureRequest 请求
     * @param loginUser                 用户
     * @return 修改数量
     */
    Integer batchUpdatePicture(BatchUpdatePictureRequest batchUpdatePictureRequest, UserLoginVo loginUser);

    /**
     * 创建AI任务
     *
     * @param createTaskRequest 请求
     * @param loginUser         用户
     * @return 任务响应
     */
    CreateTaskResponse createAiTask(CreateTaskRequest createTaskRequest, UserLoginVo loginUser);

    /**
     * 获取AI任务进度
     *
     * @param taskId    任务ID
     * @param loginUser 用户
     * @return 任务进度
     */
    QueryTaskResponse getAiTaskProgress(String taskId, UserLoginVo loginUser);

}
