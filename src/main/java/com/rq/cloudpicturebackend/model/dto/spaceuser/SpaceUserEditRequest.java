package com.rq.cloudpicturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间用户关联
 *
 * @TableName space_user
 */
@Data
public class SpaceUserEditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}