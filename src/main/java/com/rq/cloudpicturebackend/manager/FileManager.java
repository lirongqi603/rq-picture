package com.rq.cloudpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.rq.cloudpicturebackend.config.CosClientConfig;
import com.rq.cloudpicturebackend.exception.BusinessException;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import com.rq.cloudpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件管理
 *
 * @Deprecated 已废弃，改用模版方法
 */
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * 上传图片
     *
     * @param multipartFile 图片文件
     * @param uploadPrefix  图片上传前缀
     * @return 图片上传结果
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPrefix) {
        // 校验文件
        validateFile(multipartFile);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String filePath = String.format("%s/%s", uploadPrefix, uploadFileName);
        //上传文件到对象存储，并将结果返回
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传到对象存储错误");
        } finally {
            deleteTempFile(file);
        }

    }

    private void deleteTempFile(File file) {
        if (file != null) {
            boolean result = file.delete();
            if (!result) {
                log.error("临时文件删除失败,filePath = " + file.getAbsolutePath());
            }
        }
    }

    /**
     * 校验文件
     *
     * @param file 文件
     */
    private void validateFile(MultipartFile file) {
        ThrowUtils.throwIf(file == null, ErrorCode.SYSTEM_ERROR, "文件不能为空");
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(file.getSize() > ONE_MB * 2, ErrorCode.SYSTEM_ERROR, "文件大小超过限制");
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        // 只允许上传 jpg、jpeg、png、webp 格式的图片
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAM_ERROR, "文件格式不支持");
    }

    /**
     * 通过url获取图片信息并上传图片返回图片信息
     */
    public UploadPictureResult uploadByUrl(String url, String uploadPrefix) {
        validateUrl(url);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = FileUtil.mainName(url);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String filePath = String.format("%s/%s", uploadPrefix, uploadFileName);
        //上传文件到对象存储，并将结果返回
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            HttpUtil.downloadFile(url, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传到对象存储错误");
        } finally {
            deleteTempFile(file);
        }
    }

    private void validateUrl(String url) {
        //1.检验url必填
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAM_ERROR, "url不能为空");
        //2.检验url格式
        ThrowUtils.throwIf(!url.startsWith("https://") && !url.startsWith("https://"), ErrorCode.PARAM_ERROR, "仅支持https://或https://开头的url");
        //3.检验url是否有效
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            ThrowUtils.throwIf(true, ErrorCode.PARAM_ERROR, "url格式不正确");
        }
        //4.发送HEAD请求，检验url是否有效
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, url).execute();
            //4.1校验状态码是否正常
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            //4.2校验响应头是否包含Content-Type
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                //4.3校验Content-Type是否为image/jpeg、image/png、image/webp
                final List<String> ALLOW_FORMAT_LIST = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(contentType.toLowerCase()), ErrorCode.PARAM_ERROR, "文件格式不支持");
            }
            //4.3校验文件大小
            String contentLength = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLength)) {
                try {
                    long fileSize = Long.parseLong(contentLength);
                    ThrowUtils.throwIf(fileSize > 2 * 1024 * 1024, ErrorCode.PARAM_ERROR, "文件大小超过限制");
                } catch (NumberFormatException e) {
                    log.error("文件大小解析失败", e);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件大小解析失败");
                }
            }

        } catch (Exception e) {
            log.error("HEAD请求失败", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }
}
