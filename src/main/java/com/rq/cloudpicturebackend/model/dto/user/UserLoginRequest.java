package com.rq.cloudpicturebackend.model.dto.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -7481956350616805241L;
    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true, position = 1)
    private String userAccount;

    /**
     * 用户密码
     */
    @ApiModelProperty(value = "用户密码", required = true, position = 2)
    private String userPassword;

}
