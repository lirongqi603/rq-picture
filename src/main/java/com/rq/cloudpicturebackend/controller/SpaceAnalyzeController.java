package com.rq.cloudpicturebackend.controller;

import com.rq.cloudpicturebackend.annotation.AuthCheck;
import com.rq.cloudpicturebackend.common.BaseResponse;
import com.rq.cloudpicturebackend.common.ResultUtils;
import com.rq.cloudpicturebackend.constant.UserConstant;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.model.analysis.*;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.SpaceAnalyzeService;
import com.rq.cloudpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/spaceAnalyze")
public class SpaceAnalyzeController {

    @Resource
    private SpaceAnalyzeService spaceAnalysisService;

    @Resource
    private UserService userService;

    /**
     * 空间使用情况分析
     */
    @PostMapping("/spaceUsage")
    public BaseResponse<SpaceUsageAnalyzeResponse> spaceUsage(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalysisRequest,
                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalysisRequest == null, ErrorCode.PARAM_ERROR);
        UserLoginVo loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse response = spaceAnalysisService.spaceUsage(spaceUsageAnalysisRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 空间图片分类情况分析
     */
    @PostMapping("/spaceImageCategory")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> spaceImageCategory(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
                                                                               HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        UserLoginVo loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> response = spaceAnalysisService.spaceImageCategory(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 空间图片标签情况分析
     */
    @PostMapping("/spaceImageTag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> spaceImageTag(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
                                                                     HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        UserLoginVo loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> response = spaceAnalysisService.spaceImageTag(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 空间图片大小查询
     */
    @PostMapping("/spaceImageSize")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> spaceImageSize(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
                                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        UserLoginVo loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> response = spaceAnalysisService.spaceImageSize(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 用户上传行为分析
     */
    @PostMapping("/userUploadBehavior")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> userUploadBehavior(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest,
                                                                           HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        UserLoginVo loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> response = spaceAnalysisService.userUploadBehavior(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 空间使用情况排行
     */
    @PostMapping("/spaceUsageRank")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<Space>> spaceUsageRank(@RequestBody SpaceRankAnalyzeRequest spaceUsageRankAnalyzeRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageRankAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        UserLoginVo loginUser = userService.getLoginUser(request);
        List<Space> response = spaceAnalysisService.spaceUsageRank(spaceUsageRankAnalyzeRequest, loginUser);
        return ResultUtils.success(response);
    }

}
