package com.rq.cloudpicturebackend.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户详情信息
 */
@Data
public class UserInfoVo implements Serializable {
    private static final long serialVersionUID = -3037421782278293750L;
    /**
     * id
     */
    @ApiModelProperty(value = "id", position = 1)
    private Long id;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号", position = 2)
    private String userAccount;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称", position = 3)
    private String userName;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像", position = 4)
    private String userAvatar;

    /**
     * 用户简介
     */
    @ApiModelProperty(value = "用户简介", position = 5)
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @ApiModelProperty(value = "用户角色：user/admin", position = 6)
    private String userRole;

}
