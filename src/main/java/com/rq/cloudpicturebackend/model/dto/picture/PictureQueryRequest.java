package com.rq.cloudpicturebackend.model.dto.picture;

import com.rq.cloudpicturebackend.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiOperation(value = "查询图片")
public class PictureQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 8007879001456531697L;

    /**
     * 图片id
     */
    @ApiModelProperty(value = "图片id", position = 1)
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
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", position = 6)
    private Long userId;

    /**
     * 关键字搜索
     */
    @ApiModelProperty(value = "关键字搜索", position = 7)
    private String searchText;


    /**
     * 图片体积
     */
    @ApiModelProperty(value = "图片体积", position = 8)
    private Long picSize;

    /**
     * 图片宽度
     */
    @ApiModelProperty(value = "图片宽度", position = 9)
    private Integer picWidth;

    /**
     * 图片高度
     */
    @ApiModelProperty(value = "图片高度", position = 10)
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    @ApiModelProperty(value = "图片宽高比例", position = 11)
    private Double picScale;

    /**
     * 图片格式
     */
    @ApiModelProperty(value = "图片格式", position = 12)
    private String picFormat;
}
