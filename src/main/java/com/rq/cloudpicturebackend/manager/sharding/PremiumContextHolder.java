package com.rq.cloudpicturebackend.manager.sharding;

public class PremiumContextHolder {
    private static final ThreadLocal<Boolean> PREMIUM_FLAG = new ThreadLocal<>();

    public static void setPremium(Boolean isPremium) {
        PREMIUM_FLAG.set(isPremium);
    }

    public static Boolean isPremium() {
        Boolean flag = PREMIUM_FLAG.get();
        return flag != null && flag; // 默认 false（非旗舰版）
    }

    public static void clear() {
        PREMIUM_FLAG.remove();
    }
}