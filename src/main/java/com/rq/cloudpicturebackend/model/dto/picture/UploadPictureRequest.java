package com.rq.cloudpicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UploadPictureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty(value = "id", position = 1)
    private Long id;

    /**
     * 图片url
     */
    @ApiModelProperty(value = "图片url", required = true, position = 1)
    private String url;
}
