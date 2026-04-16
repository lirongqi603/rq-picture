package com.rq.cloudpicturebackend.api.aliyun.model;

import lombok.Data;

@Data
public class CreateTaskResponse {
    private Output output;
    private String request_id;
    private String code;      // 可选，错误时返回
    private String message;   // 可选，错误时返回

    @Data
    public static class Output {
        private String task_id;       // 重要！后续查询任务需要
        private String task_status;   // 可能为 "PENDING"
    }
}