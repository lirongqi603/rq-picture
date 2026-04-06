package com.rq.cloudpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FileUploadPictureImpl extends UploadPictureTemplate {


    @Override
    protected void validateInputSource(Object inputSource, Boolean ignoreSize) {
        MultipartFile file = (MultipartFile) inputSource;
        ThrowUtils.throwIf(file == null, ErrorCode.SYSTEM_ERROR, "文件不能为空");
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(file.getSize() > ONE_MB * 2 && !ignoreSize, ErrorCode.SYSTEM_ERROR, "文件大小超过限制");
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        // 只允许上传 jpg、jpeg、png、webp 格式的图片
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAM_ERROR, "文件格式不支持");
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        try {
            multipartFile.transferTo(file);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }

    }
}
