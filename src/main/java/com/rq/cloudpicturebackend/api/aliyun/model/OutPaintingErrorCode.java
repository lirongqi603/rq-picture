package com.rq.cloudpicturebackend.api.aliyun.model;

import lombok.Getter;

/**
 * 阿里云百炼图像扩展API错误码枚举
 */
@Getter
public enum OutPaintingErrorCode {

    /**
     * 输入json错误
     */
    INVALID_PARAMETER_JSON_PHRASE(400, "InvalidParameter.JsonPhrase", "input json error", "输入json错误"),

    /**
     * 输入图像下载失败
     */
    INVALID_PARAMETER_FILE_DOWNLOAD(400, "InvalidParameter.FileDownload", "oss download error", "输入图像下载失败"),

    /**
     * 读取图像失败
     */
    INVALID_PARAMETER_IMAGE_FORMAT(400, "InvalidParameter.ImageFormat", "read image error", "读取图像失败"),

    /**
     * 图像内容不合规
     */
    INVALID_PARAMETER_IMAGE_CONTENT(400, "InvalidParameter.ImageContent",
            "The image content does not comply with green network verification", "图像内容不合规"),

    /**
     * 输入参数值超出范围
     */
    INVALID_PARAMETER_DATA_INSPECTION(400, "InvalidParameter.DataInspection",
            "the parameters must conform to the specification: xxx", "输入参数值超出范围"),

    /**
     * 算法错误
     */
    INTERNAL_ERROR_ALGO(500, "InternalError.Algo", "algorithm process error", "算法错误"),

    /**
     * 文件上传失败
     */
    INTERNAL_ERROR_FILE_UPLOAD(500, "InternalError.FileUpload", "oss upload error", "文件上传失败");

    private final Integer httpStatusCode;
    private final String code;
    private final String message;
    private final String description;

    OutPaintingErrorCode(Integer httpStatusCode, String code, String message, String description) {
        this.httpStatusCode = httpStatusCode;
        this.code = code;
        this.message = message;
        this.description = description;
    }

    /**
     * 根据接口错误码code获取对应的枚举
     *
     * @param code 接口错误码，如 "InvalidParameter.JsonPhrase"
     * @return 对应的枚举，未找到时返回null
     */
    public static OutPaintingErrorCode fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (OutPaintingErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * 根据HTTP状态码和接口错误码判断是否为特定错误（可选辅助方法）
     */
    public static OutPaintingErrorCode fromHttpAndCode(Integer httpStatusCode, String code) {
        OutPaintingErrorCode errorCode = fromCode(code);
        if (errorCode != null && errorCode.getHttpStatusCode().equals(httpStatusCode)) {
            return errorCode;
        }
        return null;
    }
}