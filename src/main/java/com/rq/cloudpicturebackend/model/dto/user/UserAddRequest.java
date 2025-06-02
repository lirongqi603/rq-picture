package com.rq.cloudpicturebackend.model.dto.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户新增
 */
@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = -6125751122227845144L;
    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true, position = 1)
    private String userAccount;

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

    /**
     * 用户角色：user/admin
     */
    @ApiModelProperty(value = "用户角色：user/admin", required = true, position = 5)
    private String userRole;

}
