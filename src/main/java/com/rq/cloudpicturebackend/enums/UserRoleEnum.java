package com.rq.cloudpicturebackend.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 李荣琦
 * @Date: 2025/06/01/22:36
 * @Description:
 */
@Getter
public enum UserRoleEnum {

    ADMIN("管理员", "admin"),
    USER("普通用户", "user");

    private String text;

    private String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByCode(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
