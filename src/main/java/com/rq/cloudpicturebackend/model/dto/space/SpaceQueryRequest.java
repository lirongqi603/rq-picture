package com.rq.cloudpicturebackend.model.dto.space;

import com.baomidou.mybatisplus.annotation.TableField;
import com.rq.cloudpicturebackend.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "id", position = 1)
    private Long id;


    /**
     * 空间名称
     */
    @ApiModelProperty(value = "空间名称", position = 2)
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @ApiModelProperty(value = "空间级别：0-普通版 1-专业版 2-旗舰版", position = 3)
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @ApiModelProperty(value = "空间图片的最大总大小", position = 4)
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @ApiModelProperty(value = "空间图片的最大数量", position = 5)
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    @ApiModelProperty(value = "当前空间下图片的总大小", position = 6)
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    @ApiModelProperty(value = "当前空间下的图片数量", position = 7)
    private Long totalCount;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id", position = 8)
    private Long userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}