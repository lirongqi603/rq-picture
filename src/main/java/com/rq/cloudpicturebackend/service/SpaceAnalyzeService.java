package com.rq.cloudpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.cloudpicturebackend.model.analysis.*;
import com.rq.cloudpicturebackend.model.entity.Picture;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;

import java.util.List;

public interface SpaceAnalyzeService extends IService<Picture> {

    SpaceUsageAnalyzeResponse spaceUsage(SpaceUsageAnalyzeRequest spaceUsageAnalysisRequest, UserLoginVo loginUser);

    List<SpaceCategoryAnalyzeResponse> spaceImageCategory(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, UserLoginVo loginUser);

    List<SpaceTagAnalyzeResponse> spaceImageTag(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, UserLoginVo loginUser);

    List<SpaceSizeAnalyzeResponse> spaceImageSize(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, UserLoginVo loginUser);

    List<SpaceUserAnalyzeResponse> userUploadBehavior(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, UserLoginVo loginUser);

    List<Space> spaceUsageRank(SpaceRankAnalyzeRequest spaceUsageRankAnalyzeRequest, UserLoginVo loginUser);

}
