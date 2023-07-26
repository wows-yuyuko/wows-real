package com.shinoaki.wows.real.wows.game;


import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.real.wows.WowsCache;
import com.shinoaki.wows.real.wows.game.info.*;
import com.shinoaki.wows.real.wows.source.DamageData;

/**
 * 战斗数据信息
 *
 * @author Xun
 * @date 2023/5/14 17:32 星期日
 */
public record BattleInfoData(

        BattleInfo battleInfo,

        AvgInfo avgInfo,

        FragsInfo fragsInfo,

        MaxInfo maxInfo,

        ControlCapturedAndDroppedPointsInfo controlCapturedAndDroppedPointsInfo,

        HitRatioInfo hitRatioInfo,

        long lastBattleTime,

        long recordTime
) {
    public static BattleInfoData to(ShipInfo info) {
        return new BattleInfoData(
                BattleInfo.to(info),
                AvgInfo.to(info),
                FragsInfo.to(info),
                MaxInfo.to(info),
                ControlCapturedAndDroppedPointsInfo.to(info),
                HitRatioInfo.to(info),
                info.lastBattleTime(),
                info.recordTime()
        );
    }

    public static BattleInfoData empty(long shipId) {
        return new BattleInfoData(new BattleInfo(0, 0, 0, 0, 0),
                new AvgInfo(0, DamageData.empty(), 0, 0, WowsCache.getWins(0), 0, 0, 0, 0, 0, 0, 0, 0),
                new FragsInfo(0, 0, 0, 0, 0, 0, 0),
                MaxInfo.empty(shipId),
                new ControlCapturedAndDroppedPointsInfo(0, 0),
                new HitRatioInfo(0, 0, 0, 0), 0, 0);
    }
}
