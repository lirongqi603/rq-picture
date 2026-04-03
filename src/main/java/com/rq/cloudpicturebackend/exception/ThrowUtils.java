package com.rq.cloudpicturebackend.exception;

/**
 * 抛出异常工具类
 */

public class ThrowUtils {

    /**
     * 条件判断抛出异常
     *
     * @param condition        条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件判断抛出异常
     *
     * @param condition 条件
     * @param errorCode 自定义异常
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, errorCode, errorCode.getMessage());
    }

    /**
     * 条件判断抛出异常
     *
     * @param condition 条件
     * @param errorCode 自定义异常
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
