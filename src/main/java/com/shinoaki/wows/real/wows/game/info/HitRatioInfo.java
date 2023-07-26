package com.shinoaki.wows.real.wows.game.info;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.real.wows.WowsCache;

/**
 * 命中信息
 *
 * @author Xun
 * @date 2023/5/14 17:49 星期日
 */
public record HitRatioInfo(

        double ratioMain,

        double ratioAtba,

        double ratioTpd,

        double ratioTbomb
) {

    public static HitRatioInfo to(ShipInfo info) {
        return new HitRatioInfo(
                WowsCache.doubleCheckAnd_HALF_UP(info.ratioMain().hitRatio()),
                WowsCache.doubleCheckAnd_HALF_UP(info.ratioAtba().hitRatio()),
                WowsCache.doubleCheckAnd_HALF_UP(info.ratioTpd().hitRatio()),
                WowsCache.doubleCheckAnd_HALF_UP(info.ratioTbomb().hitRatio())
        );
    }
}
