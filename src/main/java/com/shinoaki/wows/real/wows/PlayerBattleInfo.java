package com.shinoaki.wows.real.wows;

import java.util.List;

/**
 * @author Xun
 * create or update time = 2023/7/25 17:12 星期二
 */
public record PlayerBattleInfo(String battleType,

                               long time,

                               List<BattleInfo> infoList) {

    public record BattleInfo(

            String server,
            /**
             * 机器人是0
             */
            long accountId,

            String userName,

            long shipId,

            boolean hidden,

            long clanId,

            String tag,

            int relation) {

    }
}
