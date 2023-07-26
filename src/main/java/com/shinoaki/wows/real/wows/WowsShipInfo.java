package com.shinoaki.wows.real.wows;


/**
 * @author Xun
 * @date 2023/5/15 15:20 星期一
 */
public record WowsShipInfo(

        long shipId,

        String nameCn,

        String nameEnglish,

        String nameNumbers,

        int level,

        String shipType,

        String country,

        String imgSmall,

        String imgLarge,

        String imgMedium,

        String shipIndex,

        String groupType
) {

    public static WowsShipInfo empty(long shipId) {
        return new WowsShipInfo(shipId, "未知战舰", "none", "none", 0, "none", "none", null, null, null, "none", "none");
    }
}
