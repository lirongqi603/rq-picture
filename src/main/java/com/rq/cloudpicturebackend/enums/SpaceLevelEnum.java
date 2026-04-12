package com.rq.cloudpicturebackend.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum SpaceLevelEnum {

    REGULAR("普通版", 0, 100L, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000L, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 10000L, 10000L * 1024 * 1024);

    private String text;

    private Integer value;

    private Long count;

    private Long size;

    SpaceLevelEnum(String text, Integer value, Long count, Long size) {
        this.text = text;
        this.value = value;
        this.count = count;
        this.size = size;
    }

    public static SpaceLevelEnum getEnumByCode(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
            if (Objects.equals(spaceLevelEnum.value, value)) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
