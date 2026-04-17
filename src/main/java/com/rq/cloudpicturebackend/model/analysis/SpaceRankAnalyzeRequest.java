package com.rq.cloudpicturebackend.model.analysis;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceRankAnalyzeRequest extends SpaceAnalyzeRequest implements Serializable {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}
