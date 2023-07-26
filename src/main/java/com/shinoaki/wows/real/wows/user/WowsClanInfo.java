package com.shinoaki.wows.real.wows.user;

/**
 * @author Xun
 * @date 2023/5/14 15:21 星期日
 */
public record WowsClanInfo(

        long clanId,

        String tag,


        String name,

        String description,

        String color,

        int activeLevel
) {
    public static WowsClanInfo empty() {
        return new WowsClanInfo(0, "", "", "", "#FFFAFA", -1);
    }

    public static WowsClanInfo clanNoColor(long clanId, String tag, String name, String description) {
        return new WowsClanInfo(clanId, tag, name, description, "#FFFAFA", -1);
    }
}
