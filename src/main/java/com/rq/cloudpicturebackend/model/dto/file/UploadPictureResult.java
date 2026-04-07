package com.rq.cloudpicturebackend.model.dto.file;

import lombok.Data;

/**
 * @author lirongqi
 * @description 图片上传结果 DTO
 * @date 2026-04-03
 */
@Data
public class UploadPictureResult {

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String picName;
    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 缩略图地址
     */
    private String thumbnailUrl;
}
