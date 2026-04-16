package com.rq.cloudpicturebackend.api.aliyun.model;

import lombok.Data;

@Data
public class QueryTaskResponse {
    private Output output;
    private String request_id;
    private String code;
    private String message;

    @Data
    public static class Output {
        private String task_id;
        private String task_status;          // PENDING, RUNNING, SUCCEEDED, FAILED
        private String output_image_url;     // 任务成功时返回
        private String submit_time;
        private String end_time;
        private TaskMetrics task_metrics;

        @Data
        public static class TaskMetrics {
            private Integer TOTAL;
            private Integer SUCCEEDED;
            private Integer FAILED;
        }
    }
}