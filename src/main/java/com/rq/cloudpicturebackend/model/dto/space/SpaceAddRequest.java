package com.rq.cloudpicturebackend.model.dto.space;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAddRequest implements Serializable {

    /**
     * 空间名称
     */
    @ApiModelProperty(value = "空间名称", position = 1)
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @ApiModelProperty(value = "空间级别：0-普通版 1-专业版 2-旗舰版", position = 2)
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @ApiModelProperty(value = "空间图片的最大总大小", position = 3)
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @ApiModelProperty(value = "空间图片的最大数量", position = 4)
    private Long maxCount;

    /**
     * 空间类型
     */
    @ApiModelProperty(value = "空间类型", position = 5)
    private Integer spaceType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}