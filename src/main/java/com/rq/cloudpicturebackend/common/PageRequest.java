package com.rq.cloudpicturebackend.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -7515579478612049534L;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码", position = 96)
    private int current = 1;
    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数", position = 97)
    private int pageSize = 10;
    /**
     * 排序字段
     */
    @ApiModelProperty(value = "排序字段", position = 98)
    private String sortField;
    /**
     * 排序规则
     */
    @ApiModelProperty(value = "排序规则", position = 99)
    private String sortOrder = "desc";
}
