package com.rq.cloudpicturebackend.model.analysis;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAnalyzeRequest implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 空间id
     */
    @ApiModelProperty(value = "空间id", position = 1)
    private Long spaceId;

    /**
     * 是否查询全部
     */
    @ApiModelProperty(value = "是否查询全部", position = 2)
    private Boolean isAll;

    /**
     * 是否查询公共图库
     */
    @ApiModelProperty(value = "是否查询公共图库", position = 3)
    private Boolean isPublic;

}