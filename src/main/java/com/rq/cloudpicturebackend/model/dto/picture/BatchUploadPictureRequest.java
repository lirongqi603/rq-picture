package com.rq.cloudpicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BatchUploadPictureRequest implements Serializable {

    private static final long serialVersionUID = 2821086588412027631L;

    /**
     * 搜索内容
     */
    @ApiModelProperty(value = "搜索内容", position = 1)
    private String searchText;

    /**
     * 搜索页码
     */
    @ApiModelProperty(value = "搜索页码", position = 2)
    private Integer searchPage;

    /**
     * 搜索数量
     */
    @ApiModelProperty(value = "搜索数量", position = 3)
    private Integer searchNum;

    /**
     * 图片名称
     */
    @ApiModelProperty(value = "图片名称前缀", position = 4)
    private String namePrefix;

    /**
     * 图片类型
     */
    @ApiModelProperty(value = "图片类型", position = 5)
    private String category;

    /**
     * 图片标签
     */
    @ApiModelProperty(value = "图片标签", position = 6)
    private List<String> tagList;

}
