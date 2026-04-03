package com.rq.cloudpicturebackend.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureTagCategory implements Serializable {

    private static final long serialVersionUID = -4645591052051222693L;

    @ApiModelProperty(value = "标签列表", position = 1)
    private List<String> tagList;

    @ApiModelProperty(value = "分类列表", position = 2)
    private List<String> categoryList;

}
