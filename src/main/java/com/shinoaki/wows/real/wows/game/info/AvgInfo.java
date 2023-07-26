package com.shinoaki.wows.real.wows.game.info;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.real.wows.WowsCache;
import com.shinoaki.wows.real.wows.source.DamageData;
import com.shinoaki.wows.real.wows.source.WinsData;


/**
 * @author Xun
 * @date 2023/5/14 20:52 星期日
 */
public record AvgInfo(

        int damage,

        DamageData damageData,

        int scoutingDamage,

        double win,

        WinsData winsData,

        double kd,

        double frags,

        int shipsSpotted,

        int planesKilled,

        int artAgro,

        int tpdAgro,

        int xp,

        int basicXp) {

    public static AvgInfo to(ShipInfo info) {
        int damage = WowsCache.doubleCheckAnd_HALF_UP_Int(info.gameDamage());
        int wins = WowsCache.doubleCheckAnd_HALF_UP_Int(info.gameWins());
        return new AvgInfo(
                damage,
                WowsCache.getDamage(info.shipId(), damage),
                WowsCache.doubleCheckAnd_HALF_UP_Int(info.gameScoutingDamage()),
                wins,
                WowsCache.getWins(wins),
                WowsCache.doubleCheckAnd_HALF_UP(info.gameKd()),
                WowsCache.doubleCheckAnd_HALF_UP(info.gameFrags()),
                WowsCache.doubleCheckAnd_HALF_UP_Int(info.gameShipsSpotted()),
                WowsCache.doubleCheckAnd_HALF_UP_Int(info.gamePlanesKilled()),
                WowsCache.doubleCheckAnd_HALF_UP_Int(info.gameArtAgro()),
                WowsCache.doubleCheckAnd_HALF_UP_Int(info.gameTpdAgro()),
                WowsCache.doubleCheckAnd_HALF_UP_Int(info.gameXp()),
                WowsCache.doubleCheckAnd_HALF_UP_Int(info.gameBasicXp())
        );
    }
}
