package com.shinoaki.wows.real.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.pr.PrUtils;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.real.cache.SubUserCache;
import com.shinoaki.wows.real.config.type.DataType;
import com.shinoaki.wows.real.http.HttpService;
import com.shinoaki.wows.real.wows.AccountInfo;
import com.shinoaki.wows.real.wows.PlayerBattleInfo;
import com.shinoaki.wows.real.wows.WowsCache;
import com.shinoaki.wows.real.wows.game.BattleInfoData;
import com.shinoaki.wows.real.wows.global.PrInfo;
import com.shinoaki.wows.real.wows.service.CacheShipMap;
import com.shinoaki.wows.real.wows.service.WowsHttpData;
import com.shinoaki.wows.real.wows.user.WowsUserInfo;
import com.shinoaki.wows.real.wows.view.UserShipInfoView;
import com.shinoaki.wows.real.ws.data.BaseWsPackage;
import com.shinoaki.wows.real.ws.data.WsPathType;
import com.shinoaki.wows.real.ws.server.handler.ChannelEventsHandler;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Xun
 * create or update time = 2023/7/25 18:52 星期二
 */
public class WsService {
    private WsService() {
    }

    private static final Logger log = LoggerFactory.getLogger(WsService.class);
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * user_accountId
     *
     * @param path
     * @param dataJson
     */
    public static void select(ChannelId channelId, String path, String dataJson) {
        JsonUtils json = new JsonUtils();
        try {
            switch (path) {
                case "user_accountId" -> SubUserCache.put(channelId, json.parse(dataJson, new TypeReference<Set<AccountInfo>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                }));
            }
        } catch (Exception e) {
            log.error("ws 路由处理服务异常!", e);
        }
    }

    /**
     * 推送对局战绩
     *
     * @param battleInfo 对局战绩信息
     * @param map        待推送用户信息
     */
    public static void playerGame(PlayerBattleInfo battleInfo, Map<ChannelId, AccountInfo> map) {
        EXECUTOR_SERVICE.submit(() -> {
            try {
                //处理与计算
                JsonUtils json = new JsonUtils();
                for (var kv : map.entrySet()) {
                    sendMsg(kv.getKey(), json.toJson(battleInfo));
                }
            } catch (Exception e) {
                log.error("计算战绩信息服务异常!", e);
            }
        });
    }

    public static void userPlayerOnShip(WowsHttpData wowsHttpData, long accountId) {
        //先判断v1版本数据,如果v1版本数据有变动则进入v2版本判断,仅限vortex模式时
        EXECUTOR_SERVICE.submit(() -> {
            try {
                CacheShipMap dataV1 = WowsUserService.dataV1(wowsHttpData, accountId);
                if (wowsHttpData.dataType() == DataType.VORTEX) {
                    if ((!dataV1.emptyFile()) && dataV1.shipMap().isEmpty()) {
                        return;
                    }
                    dataV1 = WowsUserService.dataV2(wowsHttpData, accountId);
                }
                if (dataV1.shipMap().isEmpty()) {
                    return;
                }
                WowsUserInfo userInfo = WowsUserService.userInfo(wowsHttpData, accountId);
                for (Map.Entry<WowsBattlesType, List<ShipInfo>> entry : dataV1.shipMap().entrySet()) {
                    WowsBattlesType k = entry.getKey();
                    if (k == WowsBattlesType.PVP) {
                        continue;
                    }
                    List<ShipInfo> v = entry.getValue();
                    for (ShipInfo ship : v) {
                        var pr = PrInfo.pr(PrUtils.prShip(ship, WowsCache.getPr(ship.shipId())));
                        UserShipInfoView view = new UserShipInfoView(userInfo, WowsCache.getShipMap(ship.shipId()), k, pr, BattleInfoData.to(ship));
                        sendMsg(accountId, new BaseWsPackage<>(WsPathType.user_real_info.name(), view));
                        //数据推送至http服务接口
                        HttpService.queueUserPlayOne(accountId, view);
                    }
                }
            } catch (IOException e) {
                log.error("{} 推送用户个人战绩信息异常!", accountId, e);
            }
        });
    }

    private static void sendMsg(long accountId, BaseWsPackage<?> baseWsPackage) throws JsonProcessingException {
        JsonUtils json = new JsonUtils();
        for (var key : SubUserCache.checkWs(accountId).keySet()) {
            log.info("{}-{} ws推送消息... {}", key, accountId, baseWsPackage.getPath());
            sendMsg(key, json.toJson(baseWsPackage));
        }
    }

    private static void sendMsg(ChannelId id, String msg) {
        ChannelEventsHandler.sendMsg(id, msg);
    }
}
