package com.shinoaki.wows.real.wows.view;

import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.real.wows.WowsShipInfo;
import com.shinoaki.wows.real.wows.game.BattleInfoData;
import com.shinoaki.wows.real.wows.global.PrInfo;
import com.shinoaki.wows.real.wows.user.WowsUserInfo;

/**
 * @author Xun
 * create or update time = 2023/7/25 22:45 星期二
 */
public record UserShipInfoView(WowsUserInfo userInfo, WowsShipInfo shipInfo, WowsBattlesType battlesType, PrInfo prInfo, BattleInfoData battleInfo) {
}
