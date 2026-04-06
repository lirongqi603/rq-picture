package com.rq.cloudpicturebackend.model.dto.picture;

import com.rq.cloudpicturebackend.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiOperation(value = "审核图片")
public class PictureReviewRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 8007879001456531697L;

    /**
     * 图片id
     */
    @ApiModelProperty(value = "图片id", position = 1)
    private Long id;


    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    @ApiModelProperty(value = "审核状态", position = 2)
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @ApiModelProperty(value = "审核信息", position = 3)
    private String reviewMessage;
}
