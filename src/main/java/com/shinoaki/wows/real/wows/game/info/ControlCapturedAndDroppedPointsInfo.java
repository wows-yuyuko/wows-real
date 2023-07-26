package com.shinoaki.wows.real.wows.game.info;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.real.wows.WowsCache;


/**
 * 站点信息
 *
 * @author Xun
 * @date 2023/5/14 17:50 星期日
 */
public record ControlCapturedAndDroppedPointsInfo(

        double gameContributionToCapture,

        double gameContributionToDefense) {

    public static ControlCapturedAndDroppedPointsInfo to(ShipInfo info) {
        return new ControlCapturedAndDroppedPointsInfo(
                WowsCache.doubleCheckAnd_HALF_UP(info.controlCapturedAndDroppedPoints().gameContributionToCapture()),
                WowsCache.doubleCheckAnd_HALF_UP(info.controlCapturedAndDroppedPoints().gameContributionToDefense()));
    }
}
