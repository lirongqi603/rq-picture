package com.rq.cloudpicturebackend.api.aliyun;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.rq.cloudpicturebackend.api.aliyun.model.CreateTaskRequest;
import com.rq.cloudpicturebackend.api.aliyun.model.CreateTaskResponse;
import com.rq.cloudpicturebackend.api.aliyun.model.OutPaintingErrorCode;
import com.rq.cloudpicturebackend.api.aliyun.model.QueryTaskResponse;
import com.rq.cloudpicturebackend.exception.BusinessException;
import com.rq.cloudpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ImageOutPaintingClient {
    private static final String CREATE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    private static final String TASK_BASE_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/";

    @Value("${alibailian.apiKey}")
    private String apiKey;

    public CreateTaskResponse createTask(CreateTaskRequest request) {
        HttpResponse response = null;
        try {
            response = HttpRequest.post(CREATE_TASK_URL)
                    .header(Header.AUTHORIZATION, "Bearer sk-eb77ec14c1914b118ef36772bdf6793f")
                    .header("X-DashScope-Async", "enable")
                    .header(Header.CONTENT_TYPE, "application/json")
                    .body(JSONUtil.toJsonStr(request))
                    .execute();
            if (response.isOk()) {
                String body = response.body();
                return JSONUtil.toBean(body, CreateTaskResponse.class);
            } else if (response.getStatus() == HttpStatus.HTTP_BAD_REQUEST) {
                String body = response.body();
                Map map = JSONUtil.toBean(body, Map.class);
                String code = (String) map.get("code");
                OutPaintingErrorCode outPaintingErrorCode = OutPaintingErrorCode.fromCode(code);
                if (outPaintingErrorCode != null) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, outPaintingErrorCode.getMessage());
                } else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建任务失败");
                }
            }
        } catch (Exception e) {
            log.error("createTask error", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        } finally {
            if (response != null)
                response.close();
        }
        return null;
    }


    public QueryTaskResponse queryTask(String taskId) {
        String url = TASK_BASE_URL + taskId;
        String responseBody = HttpRequest.get(url)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .execute()
                .body();
        return JSONUtil.toBean(responseBody, QueryTaskResponse.class);
    }

    public static void main(String[] args) {
        ImageOutPaintingClient client = new ImageOutPaintingClient();
        CreateTaskRequest request = new CreateTaskRequest();
        request.setInput(new CreateTaskRequest.Input());
        request.getInput().setImage_url("https://cloud-picture-1362987934.cos.ap-beijing.myqcloud.com/public/1929208199826354177/2026-04-14_57decbb34djubzff.webp");
        request.setParameters(new CreateTaskRequest.Parameters());
        request.getParameters().setX_scale(2.0f);
        request.getParameters().setY_scale(2.0f);
        System.out.println(JSONUtil.toJsonStr(request));
        System.out.println(client.createTask(request));
    }
}