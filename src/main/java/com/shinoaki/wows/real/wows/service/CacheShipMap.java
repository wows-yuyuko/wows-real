package com.shinoaki.wows.real.wows.service;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.type.WowsBattlesType;

import java.util.List;
import java.util.Map;

/**
 * @author Xun
 * create or update time = 2023/7/26 18:19 星期三
 */
public record CacheShipMap(boolean emptyFile, Map<WowsBattlesType, List<ShipInfo>> shipMap) {
}
