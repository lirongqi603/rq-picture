package com.rq.cloudpicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求类
 */
@Data
public class DeletedRequest implements Serializable {

    private static final long serialVersionUID = -4531766729095357105L;

    /**
     * 唯一ID
     */
    private long id;
}
