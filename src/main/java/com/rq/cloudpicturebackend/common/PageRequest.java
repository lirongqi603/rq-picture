package com.rq.cloudpicturebackend.common;

import java.io.Serializable;

/**
 * 分页请求
 */
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -7515579478612049534L;

    /**
     * 页码
     */
    private int current = 1;
    /**
     * 每页条数
     */
    private int pageSize = 1;
    /**
     * 排序字段
     */
    private String sortField;
    /**
     * 排序规则
     */
    private String sortOrder = "descend";
}
