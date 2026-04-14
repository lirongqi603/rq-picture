package com.rq.cloudpicturebackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SpaceLevelVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 空间等级名称
     */
    private String name;

    /**
     * 空间等级值
     */
    private Integer value;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 最大空间大小
     */
    private Long maxSize;


}