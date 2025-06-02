package com.rq.cloudpicturebackend.model.dto.user;

import com.rq.cloudpicturebackend.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询
 */
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 8692855092733859708L;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号", position = 1)
    private String userAccount;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称", position = 2)
    private String userName;
}

