package com.rq.cloudpicturebackend.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户列表
 */
@Data
public class UserQueryListVo implements Serializable {
    private static final long serialVersionUID = -6786282213441906422L;
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

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", position = 7)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


}
