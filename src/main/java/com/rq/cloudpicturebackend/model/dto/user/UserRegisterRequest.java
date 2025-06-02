package com.rq.cloudpicturebackend.model.dto.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求Dto
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -3059984216125771678L;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号", position = 1, required = true)
    private String userAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", position = 2, required = true)
    private String userPassword;

    /**
     * 确认密码
     */
    @ApiModelProperty(value = "确认密码", position = 3, required = true)
    private String checkPassword;
}
