package com.rq.cloudpicturebackend.model.dto.space;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceEditRequest implements Serializable {

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}