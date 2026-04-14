package com.rq.cloudpicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lirongqi
 * @date 2026-04-03
 * @description 图片上传 DTO
 */

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = -6926365819463971351L;

    /**
     * 图片id
     */
    @ApiModelProperty(value = "图片id", position = 1)
    private Long id;

    /**
     * 图片url
     */
    @ApiModelProperty(value = "图片地址", position = 2)
    private String url;

    /**
     * 图片名称
     */
    @ApiModelProperty(value = "图片名称", position = 3)
    private String name;

    /**
     * 图片类型
     */
    @ApiModelProperty(value = "图片类型", position = 4)
    private String category;

    /**
     * 图片标签
     */
    @ApiModelProperty(value = "图片标签", position = 5)
    private List<String> tagList;

    /**
     * 图片空间id
     */
    @ApiModelProperty(value = "图片空间id", position = 6)
    private Long spaceId;
}
