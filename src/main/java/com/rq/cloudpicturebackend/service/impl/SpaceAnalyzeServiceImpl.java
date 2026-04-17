package com.rq.cloudpicturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.cloudpicturebackend.constant.UserConstant;
import com.rq.cloudpicturebackend.exception.BusinessException;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.mapper.PictureMapper;
import com.rq.cloudpicturebackend.model.analysis.*;
import com.rq.cloudpicturebackend.model.entity.Picture;
import com.rq.cloudpicturebackend.model.entity.Space;
import com.rq.cloudpicturebackend.model.vo.UserLoginVo;
import com.rq.cloudpicturebackend.service.SpaceAnalyzeService;
import com.rq.cloudpicturebackend.service.SpaceService;
import com.rq.cloudpicturebackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<PictureMapper, Picture> implements SpaceAnalyzeService {

    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;

    @Override
    public SpaceUsageAnalyzeResponse spaceUsage(SpaceUsageAnalyzeRequest spaceUsageAnalysisRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(spaceUsageAnalysisRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //鉴权
        checkAuth(spaceUsageAnalysisRequest, loginUser);
        if (spaceUsageAnalysisRequest.getIsAll() || spaceUsageAnalysisRequest.getIsPublic()) {
            //补充查询条件
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            fullSpaceQueryWrapper(queryWrapper, spaceUsageAnalysisRequest);
            queryWrapper.select("picSize");
            List<Object> list = this.baseMapper.selectObjs(queryWrapper).stream()
                    .filter(ObjUtil::isNotEmpty)
                    .collect(Collectors.toList());
            long count = list.size();
            long usedSize = list.stream().mapToLong(data -> data instanceof Long ? (Long) data : 0).sum();
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(usedSize);
            response.setUsedCount(count);
            return response;
        } else if (spaceUsageAnalysisRequest.getSpaceId() != null) {
            Space space = spaceService.getById(spaceUsageAnalysisRequest.getSpaceId());
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(space.getTotalSize());
            response.setUsedCount(space.getTotalCount());
            response.setMaxSize(space.getMaxSize());
            response.setMaxCount(space.getMaxCount());
            double sizeRatio = NumberUtil.round((space.getTotalSize() * 100.0 / space.getMaxSize()), 2).doubleValue();
            double countRatio = NumberUtil.round((space.getTotalCount() * 100.0 / space.getMaxCount()), 2).doubleValue();
            response.setSizeUsageRatio(sizeRatio);
            response.setCountUsageRatio(countRatio);
            return response;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "暂不支持其他查询条件");
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> spaceImageCategory(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //鉴权
        checkAuth(spaceCategoryAnalyzeRequest, loginUser);
        //补充查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fullSpaceQueryWrapper(queryWrapper, spaceCategoryAnalyzeRequest);
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize").groupBy("category");
        return this.baseMapper.selectMaps(queryWrapper).stream().map(data -> {
            SpaceCategoryAnalyzeResponse response = new SpaceCategoryAnalyzeResponse();
            response.setCategory((ObjUtil.isNull(data.get("category"))) ? "默认" : (String) data.get("category"));
            response.setCount(((Number) data.get("count")).longValue());
            response.setTotalSize(((Number) data.get("totalSize")).longValue());
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> spaceImageTag(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //鉴权
        checkAuth(spaceTagAnalyzeRequest, loginUser);
        //补充查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fullSpaceQueryWrapper(queryWrapper, spaceTagAnalyzeRequest);
        queryWrapper.select("tags");
        List<String> jsonStrList = this.baseMapper.selectObjs(queryWrapper).stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        //将json字符串展开
        Map<String, Long> dataMap = jsonStrList.stream().flatMap(jsonStr -> JSONUtil.toList(jsonStr, String.class).stream())
                .collect(Collectors.groupingBy(data -> data, Collectors.counting()));
        return dataMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> spaceImageSize(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //鉴权
        checkAuth(spaceSizeAnalyzeRequest, loginUser);
        //补充查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fullSpaceQueryWrapper(queryWrapper, spaceSizeAnalyzeRequest);
        queryWrapper.select("picSize");
        List<Long> list = this.baseMapper.selectObjs(queryWrapper).stream()
                .filter(ObjUtil::isNotEmpty)
                .map(data -> (Long) data)
                .collect(Collectors.toList());
        //范围 <100kb 100kb-500kb 500kb-1mb 1mb以上
        Map<String, Long> dataMap = new LinkedHashMap<>();
        dataMap.put("小于100KB", list.stream().filter(data -> data < 100 * 1024).count());
        dataMap.put("100KB-500KB", list.stream().filter(data -> data >= 100 * 1024 && data < 500 * 1024).count());
        dataMap.put("500KB-1MB", list.stream().filter(data -> data >= 500 * 1024 && data < 1024 * 1024).count());
        dataMap.put("1MB以上", list.stream().filter(data -> data >= 1024 * 1024).count());
        return dataMap.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> userUploadBehavior(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Long userId = spaceUserAnalyzeRequest.getUserId();
        //鉴权
        checkAuth(spaceUserAnalyzeRequest, loginUser);
        //查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        String userRole = loginUser.getUserRole();
        if (UserConstant.ADMIN_ROLE.equals(userRole) && userId != null) {
            queryWrapper.eq("userId", userId);
        } else {
            queryWrapper.eq("userId", loginUser.getId());
        }
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        //分析维度：每天，每周，每月
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime,'%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime) as period", "count(*) as count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime,'%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR, "暂不支持其他查询条件");
        }
        queryWrapper.groupBy("period").orderByAsc("period");
        return this.baseMapper.selectMaps(queryWrapper).stream()
                .map(data -> new SpaceUserAnalyzeResponse(data.get("period").toString(), (Long) data.get("count")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Space> spaceUsageRank(SpaceRankAnalyzeRequest spaceUsageRankAnalyzeRequest, UserLoginVo loginUser) {
        ThrowUtils.throwIf(spaceUsageRankAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        Integer topN = spaceUsageRankAnalyzeRequest.getTopN();
        ThrowUtils.throwIf(topN == null, ErrorCode.PARAM_ERROR);
        //补充查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize").last("limit " + topN);
        return spaceService.list(queryWrapper);
    }

    /**
     * 补充查询条件
     *
     * @param queryWrapper        查询条件
     * @param spaceAnalyzeRequest 请求
     */
    private void fullSpaceQueryWrapper(QueryWrapper<Picture> queryWrapper, SpaceAnalyzeRequest spaceAnalyzeRequest) {
        Boolean isAll = spaceAnalyzeRequest.getIsAll();
        Boolean isPublic = spaceAnalyzeRequest.getIsPublic();
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (isAll) {
            return;
        }
        if (isPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "暂不支持其他查询条件");
    }

    /**
     * 鉴权
     *
     * @param spaceAnalyzeRequest 请求
     * @param loginUser           登录用户
     */
    private void checkAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, UserLoginVo loginUser) {
        Boolean isAll = spaceAnalyzeRequest.getIsAll();
        Boolean isPublic = spaceAnalyzeRequest.getIsPublic();

        if (isAll || isPublic) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR, "无权限访问");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NOT_AUTH_ERROR, "无权限访问");
            return;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "暂不支持其他查询条件");
    }
}
