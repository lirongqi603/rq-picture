package com.rq.cloudpicturebackend.api.aliyun.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateTaskRequest {
    private String model = "image-out-painting"; // 固定模型名称
    private Input input;
    private Parameters parameters;

    @Data
    public static class Input {
        private String image_url; // 输入图片URL
    }

    @Data
    public static class Parameters {
        // 1. 按比例扩图
        @JsonProperty("xScale")
        private Float x_scale;
        @JsonProperty("yScale")
        private Float y_scale;
        // 2. 按像素扩图
        private Integer top_offset;
        private Integer bottom_offset;
        private Integer left_offset;
        private Integer right_offset;
        // 3. 旋转与宽高比
        private Integer angle;
        private String output_ratio;
        // 4. 质量控制
        private Boolean best_quality;
        private Boolean limit_image_size;
    }
}