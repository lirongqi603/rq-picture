package com.rq.cloudpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.rq.cloudpicturebackend.exception.BusinessException;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import com.rq.cloudpicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UrlUploadPictureImpl extends UploadPictureTemplate {


    @Override
    protected void validateInputSource(Object inputSource, Boolean ignoreSize) {
        String url = (String) inputSource;
        //1.检验url必填
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAM_ERROR, "url不能为空");
        //2.检验url格式
        ThrowUtils.throwIf(!url.startsWith("http://") && !url.startsWith("https://"), ErrorCode.PARAM_ERROR, "仅支持http://或https://开头的url");
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
                    ThrowUtils.throwIf(fileSize > 2 * 1024 * 1024 && !ignoreSize, ErrorCode.PARAM_ERROR, "文件大小超过限制");
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

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String url = (String) inputSource;
        return FileUtil.getName(url);
    }

    @Override
    protected void processFile(Object inputSource, File file) {
        String url = (String) inputSource;
        HttpUtil.downloadFile(url, file);
    }
}
