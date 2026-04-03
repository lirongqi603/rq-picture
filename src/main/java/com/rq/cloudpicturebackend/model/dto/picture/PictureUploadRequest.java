package com.rq.cloudpicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lirongqi
 * @date 2026-04-03
 * @description 图片上传 DTO
 */

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = -6926365819463971351L;

    @ApiModelProperty(value = "图片id", position = 1)
    private Long id;

}
