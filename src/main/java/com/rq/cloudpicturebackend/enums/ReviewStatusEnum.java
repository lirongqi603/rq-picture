package com.rq.cloudpicturebackend.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum ReviewStatusEnum {

    PENDING("待审核", 0),
    PASS("审核通过", 1),
    REFUSE("拒绝", 2);

    private String text;

    private Integer value;

    ReviewStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    public static ReviewStatusEnum getEnumByCode(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ReviewStatusEnum userRoleEnum : ReviewStatusEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
