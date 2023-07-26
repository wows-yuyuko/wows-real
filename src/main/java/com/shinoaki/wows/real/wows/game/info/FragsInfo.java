package com.shinoaki.wows.real.wows.game.info;

import com.shinoaki.wows.api.data.ShipInfo;

/**
 * 击杀信息
 *
 * @author Xun
 * @date 2023/5/14 17:32 星期日
 */
public record FragsInfo(

        int frags,

        int fragsByMain,

        int fragsByAtba,

        int fragsByPlanes,

        int fragsByTpd,

        int fragsByRam,

        int fragsByDbomb
) {
    public static FragsInfo to(ShipInfo info) {
        return new FragsInfo(
                info.fragsInfo().frags(),
                info.fragsInfo().fragsByMain(),
                info.fragsInfo().fragsByAtba(),
                info.fragsInfo().fragsByPlanes(),
                info.fragsInfo().fragsByTpd(),
                info.fragsInfo().fragsByRam(),
                info.fragsInfo().fragsByDbomb()
        );
    }
}
