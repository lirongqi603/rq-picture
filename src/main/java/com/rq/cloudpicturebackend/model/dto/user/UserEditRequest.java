package com.rq.cloudpicturebackend.model.dto.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户新增
 */
@Data
public class UserEditRequest implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "id",position = 1)
    private Long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称", position = 2)
    private String userName;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像", position = 3)
    private String userAvatar;

    /**
     * 用户简介
     */
    @ApiModelProperty(value = "用户简介", position = 4)
    private String userProfile;

}
