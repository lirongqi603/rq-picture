package com.rq.cloudpicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiOperation(value = "修改图片请求(用户端)")
public class PictureEditRequest implements Serializable {

    private static final long serialVersionUID = 805537108365974404L;

    /**
     * 图片id
     */
    @ApiModelProperty(value = "图片id", required = true, position = 1)
    private Long id;

    /**
     * 图片名称
     */
    @ApiModelProperty(value = "图片名称", position = 2)
    private String name;

    /**
     * 简介
     */
    @ApiModelProperty(value = "简介", position = 3)
    private String introduction;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类", position = 4)
    private String category;

    /**
     * 标签
     */
    @ApiModelProperty(value = "标签", position = 5)
    private List<String> tags;

    /**
     * 空间id
     */
    @ApiModelProperty(value = "空间id", position = 6)
    private Long spaceId;
}
