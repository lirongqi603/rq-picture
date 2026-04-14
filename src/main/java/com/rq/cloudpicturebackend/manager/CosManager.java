package com.rq.cloudpicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.rq.cloudpicturebackend.config.CosClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对象存储管理类
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 唯一键
     */
    public void delObject(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(cosClientConfig.getBucket(), key);
            cosClient.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            log.error("删除对象失败", e);
        }
    }

    /**
     * 批量删除对象
     *
     * @param keys
     */
    public void delBatchObject(List<String> keys) {
        try {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
            List<DeleteObjectsRequest.KeyVersion> keyVersions = keys.stream()
                    .map(key -> new DeleteObjectsRequest.KeyVersion(key.replace(cosClientConfig.getHost() + "/", "")))
                    .collect(Collectors.toList());
            deleteObjectsRequest.setKeys(keyVersions);
            // 强烈建议：设置Quiet模式为false，以获取每个对象的详细删除结果
            deleteObjectsRequest.setQuiet(false);

            DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
            List<DeleteObjectsResult.DeletedObject> successfulDeletions = deleteObjectsResult.getDeletedObjects();
            log.info("批量删除成功，成功删除 {} 个对象。", successfulDeletions == null ? 0 : successfulDeletions.size());
        } catch (Exception e) {
            log.error("删除对象失败", e);
        }
    }

    /**
     * 上传图片
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        // 设置图片处理规则
        List<PicOperations.Rule> rules = new ArrayList<>();
        // 1.图片格式转换
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule rule1 = new PicOperations.Rule();
        rule1.setBucket(cosClientConfig.getBucket());
        rule1.setFileId(webpKey);
        rule1.setRule("imageMogr2/format/webp");
        rules.add(rule1);
        // 2.图片生成缩略图
        String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
        PicOperations.Rule rule2 = new PicOperations.Rule();
        rule2.setBucket(cosClientConfig.getBucket());
        rule2.setFileId(thumbnailKey);
        rule2.setRule("imageMogr2/thumbnail/256x256>");
        rules.add(rule2);
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
}
