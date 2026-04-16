package com.rq.cloudpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BatchUpdatePictureRequest implements Serializable {


    private static final long serialVersionUID = 705327556419147679L;

    /**
     * 图片id列表
     */
    private List<Long> pictureIdList;

    /**
     * 图片名称格式
     */
    private String nameFormat;

    /**
     * 图片类型
     */
    private String category;

    /**
     * 图片标签
     */
    private List<String> tagList;

    /**
     * 空间ID
     */
    private Long spaceId;

}
