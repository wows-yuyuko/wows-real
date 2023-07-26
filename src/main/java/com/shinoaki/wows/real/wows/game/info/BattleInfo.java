package com.shinoaki.wows.real.wows.game.info;

import com.shinoaki.wows.api.data.ShipInfo;

/**
 * @author Xun
 * @date 2023/5/14 20:56 星期日
 */
public record BattleInfo(

        int battle,

        int wins,

        int losses,

        int survived,

        int winAndSurvived
) {

    public static BattleInfo to(ShipInfo info) {
        return new BattleInfo(info.battle().battle(),
                info.battle().wins(),
                info.battle().losses(),
                info.battle().survived(),
                info.battle().winAndSurvived());
    }
}
