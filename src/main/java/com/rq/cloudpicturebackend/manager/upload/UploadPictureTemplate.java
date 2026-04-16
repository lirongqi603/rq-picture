package com.rq.cloudpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.rq.cloudpicturebackend.config.CosClientConfig;
import com.rq.cloudpicturebackend.exception.BusinessException;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.manager.CosManager;
import com.rq.cloudpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 上传图片模板
 */
@Slf4j
public abstract class UploadPictureTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * 校验参数
     *
     * @param inputSource 输入源
     * @param ignoreSize  是否忽略大小
     */
    protected abstract void validateInputSource(Object inputSource, Boolean ignoreSize);

    /**
     * 获取原始文件名
     *
     * @param inputSource 输入源
     * @return 原始文件名
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理文件
     *
     * @param inputSource 输入源
     * @param file        文件
     */
    protected abstract void processFile(Object inputSource, File file);


    /**
     * 上传图片
     *
     * @param inputSource  输入源
     * @param uploadPrefix 图片上传前缀
     * @return 图片上传结果
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPrefix, Boolean ignoreSize) {
        // 校验文件
        validateInputSource(inputSource, ignoreSize);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        //判断名称是否有？，文号及后边的的截断
        if (originalFilename.contains("?")) {
            originalFilename = originalFilename.substring(0, originalFilename.indexOf("?"));
        }
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String filePath = String.format("%s/%s", uploadPrefix, uploadFileName);
        //上传文件到对象存储，并将结果返回
        File file = null;
        try {
            file = File.createTempFile(filePath, "." + FileUtil.getSuffix(originalFilename));
            processFile(inputSource, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            List<CIObject> objectList = putObjectResult.getCiUploadResult().getProcessResults().getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                //压缩图片
                CIObject compressedCiObject = objectList.get(0);
                CIObject thumbnailCiObject = null;
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                //删除原图信息
                cosManager.delObject(filePath);
                return buildPictureResult(originalFilename, compressedCiObject, thumbnailCiObject, imageInfo);
            }
            return buildPictureResult(originalFilename, filePath, file, imageInfo);
        } catch (Exception e) {
            log.error("图片上传到对象存储错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传到对象存储错误");
        } finally {
            deleteTempFile(file);
        }

    }

    private UploadPictureResult buildPictureResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject, ImageInfo imageInfo) {
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        String thumbnailKey = compressedCiObject.getKey();
        if (thumbnailCiObject != null) {
            thumbnailKey = thumbnailCiObject.getKey();
        }
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailKey);
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;

    }


    private UploadPictureResult buildPictureResult(String originalFilename, String filePath, File file, ImageInfo imageInfo) {
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + filePath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;
    }

    private void deleteTempFile(File file) {
        if (file != null) {
            boolean result = file.delete();
            if (!result) {
                log.error("临时文件删除失败,filePath = " + file.getAbsolutePath());
            }
        }
    }


}
