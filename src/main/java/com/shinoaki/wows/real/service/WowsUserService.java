package com.shinoaki.wows.real.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.real.Main;
import com.shinoaki.wows.real.config.ProxyConfig;
import com.shinoaki.wows.real.config.type.DataType;
import com.shinoaki.wows.real.wows.service.CacheShipMap;
import com.shinoaki.wows.real.wows.service.WowsHttpData;
import com.shinoaki.wows.real.wows.user.WowsClanInfo;
import com.shinoaki.wows.real.wows.user.WowsUserInfo;

import java.io.*;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shinoaki.wows.real.Main.log;

/**
 * @author Xun
 * create or update time = 2023/7/25 21:06 星期二
 */
public class WowsUserService {
    public static final String TOKEN = "907d9c6bfc0d896a2c156e57194a97cf";
    public static final String v1 = "ship-map-v1-cache";
    public static final String v2 = "ship-map-v2-cache";

    private WowsUserService() {

    }

    public static WowsUserInfo userInfo(WowsHttpData wowsHttpData, long accountId) {
        JsonUtils json = new JsonUtils();
        try {
            //检查缓存
            HttpClient httpClient = wowsHttpData.httpClient();
            WowsUserInfo wowsUserInfo;
            WowsHttpUserTools user = new WowsHttpUserTools(json, httpClient, wowsHttpData.server());
            WowsHttpClanTools clan = new WowsHttpClanTools(json, httpClient, wowsHttpData.server());
            if (wowsHttpData.dataType() == DataType.API) {
                DevelopersUserInfo userInfo = user.userInfoDevelopers(TOKEN, accountId);
                DevelopersSearchUserClan userClan = clan.developers(TOKEN).userSearchClanDevelopers(accountId);
                WowsClanInfo clanInfo;
                if (userClan.clan_id() <= 0) {
                    clanInfo = WowsClanInfo.empty();
                } else {
                    clanInfo = WowsClanInfo.clanNoColor(userClan.clan_id(), userClan.clan().tag(), userClan.clan().name(), "");
                }
                wowsUserInfo = WowsUserInfo.user(accountId, userInfo.nickname(), wowsHttpData.server(), clanInfo, userInfo.created_at());
            } else {
                VortexSearchClanUser clanUser = clan.vortex().userSearchClanVortex(accountId);
                var clanInfo = new WowsClanInfo(clanUser.clan_id(), clanUser.clan().tag(), clanUser.clan().name(), "", clanUser.clan().color(), -1);
                List<String> userName = fileUserName(accountId);
                wowsUserInfo = WowsUserInfo.user(accountId, userName.get(0), wowsHttpData.server(), clanInfo, Long.parseLong(userName.get(1)));
            }
            return wowsUserInfo;
        } catch (StatusException | HttpStatusException | IOException e) {
            log.error("{}-{} 解析用户信息异常!", wowsHttpData.server().getCode(), accountId, e);
            return WowsUserInfo.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("{}-{} 解析用户信息异常! InterruptedException ", wowsHttpData.server().getCode(), accountId, e);
            return WowsUserInfo.empty();
        }
    }

    public static WowsHttpData http(WowsServer server) {
        File file = Main.fileConfig("proxy.json");
        JsonUtils json = new JsonUtils();
        try (FileInputStream in = new FileInputStream(file)) {
            String data = new String(in.readAllBytes());
            var proxyConfig = json.parse(data, new TypeReference<ProxyConfig>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
            return new WowsHttpData(proxyConfig, server);
        } catch (IOException e) {
            log.error("读取配置文件异常!", e);
        }
        return null;
    }

    public static CacheShipMap dataV1(WowsHttpData wowsHttpData, long accountId) throws IOException {
        return ship(v1, accountId, shipMapByPvpByRank(wowsHttpData, accountId));
    }

    public static CacheShipMap dataV2(WowsHttpData wowsHttpData, long accountId) throws IOException {
        return ship(v2, accountId, shipMap(wowsHttpData, accountId));
    }

    private static CacheShipMap ship(String versionCache, long accountId, Map<WowsBattlesType, List<ShipInfo>> shipMap) throws IOException {
        JsonUtils json = new JsonUtils();
        File file = fileShip(versionCache, accountId);
        boolean filedCreateCheck = fileCreateCheck(file);
        Map<WowsBattlesType, List<ShipInfo>> ship;
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                String data = new String(in.readAllBytes());
                ship = json.parse(data, new TypeReference<Map<WowsBattlesType, List<ShipInfo>>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                });
            }
        } else {
            ship = Map.of();
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json.toJson(shipMap));
            writer.flush();
        }
        if (ship.isEmpty()) {
            return new CacheShipMap(filedCreateCheck, ship);
        }
        return new CacheShipMap(filedCreateCheck, recent(shipMap, ship));
    }


    private static Map<WowsBattlesType, List<ShipInfo>> shipMap(WowsHttpData wowsHttpData, long accountId) {
        JsonUtils json = new JsonUtils();
        WowsHttpShipTools tools = new WowsHttpShipTools(json, wowsHttpData.httpClient(), wowsHttpData.server(), accountId);
        try {
            Map<WowsBattlesType, List<ShipInfo>> shipMap;
            if (wowsHttpData.dataType() == DataType.VORTEX) {
                shipMap = new EnumMap<>(WowsBattlesType.class);
                for (var ws : WowsBattlesType.values()) {
                    if (ws == WowsBattlesType.PVP) {
                        continue;
                    }
                    shipMap.put(ws, tools.vortex().shipList(ws).toShipInfoList());
                }
            } else {
                shipMap = tools.developers(TOKEN).shipList().toShipInfoMap();
            }
            return shipMap;
        } catch (StatusException | IOException | HttpStatusException e) {
            log.error("server={}.accountid={},请求失败!", wowsHttpData.server().getCode(), accountId, e);
        } catch (InterruptedException e) {
            log.error("server={}.accountid={},请求失败!", wowsHttpData.server().getCode(), accountId, e);
            Thread.currentThread().interrupt();
        }
        return Map.of();
    }

    /**
     * 判断用户是否有战斗记录使用,vortex模式下请求次数减少到两次,但是详细的数据计算需要走一次完整的计算(仅限vortex)
     */
    private static Map<WowsBattlesType, List<ShipInfo>> shipMapByPvpByRank(WowsHttpData wowsHttpData, long accountId) {
        JsonUtils json = new JsonUtils();
        WowsHttpShipTools tools = new WowsHttpShipTools(json, wowsHttpData.httpClient(), wowsHttpData.server(), accountId);
        try {
            Map<WowsBattlesType, List<ShipInfo>> shipMap;
            if (wowsHttpData.dataType() == DataType.VORTEX) {
                shipMap = new EnumMap<>(WowsBattlesType.class);
                String name = null;
                long createTime = 0;
                for (var ws : WowsBattlesType.values()) {
                    if (ws == WowsBattlesType.PVP || ws == WowsBattlesType.RANK_SOLO) {
                        var ship = tools.vortex().shipList(ws);
                        name = ship.name();
                        createTime = (long) ship.created_at();
                        shipMap.put(ws, ship.toShipInfoList());
                    }
                }
                fileUserName(accountId, name, createTime);
            } else {
                shipMap = tools.developers(TOKEN).shipList().toShipInfoMap();
            }
            return shipMap;
        } catch (StatusException | IOException | HttpStatusException e) {
            log.error("server={}.accountid={},请求失败!", wowsHttpData.server().getCode(), accountId, e);
        } catch (InterruptedException e) {
            log.error("server={}.accountid={},请求失败!", wowsHttpData.server().getCode(), accountId, e);
            Thread.currentThread().interrupt();
        }
        return Map.of();
    }
    private static Map<WowsBattlesType, List<ShipInfo>> recent(Map<WowsBattlesType, List<ShipInfo>> newData, Map<WowsBattlesType, List<ShipInfo>> historyData) {
        Map<WowsBattlesType, List<ShipInfo>> ship = new EnumMap<>(WowsBattlesType.class);
        newData.forEach((k, v) -> {
            List<ShipInfo> infoList = new ArrayList<>();
            var historyMap = historyData.getOrDefault(k, List.of()).stream().collect(Collectors.toMap(ShipInfo::shipId, value -> value));
            v.forEach(shipInfo -> {
                ShipInfo info = historyMap.get(shipInfo.shipId());
                if (info != null) {
                    ShipInfo temp = shipInfo.subtraction(info);
                    if (temp.battle().battle() > 0) {
                        infoList.add(temp);
                    }
                } else {
                    if (shipInfo.battle().battle() > 0) {
                        infoList.add(shipInfo);
                    }
                }
            });
            ship.put(k, infoList);
        });
        return ship;
    }

    private static List<String> fileUserName(long accountId) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileUserNameTxt(accountId)))) {
            return bufferedReader.lines().toList();
        }
    }

    private static void fileUserName(long accountId, String userName, long createTime) throws IOException {
        var f = fileUserNameTxt(accountId);
        fileCreateCheck(f);
        try (BufferedWriter buffered = new BufferedWriter(new FileWriter(f))) {
            buffered.write(userName);
            buffered.newLine();
            buffered.write(String.valueOf(createTime));
            buffered.flush();
        }
    }

    private static File fileShip(String v, long accountId) {
        return new File(Main.pathHome() + "cache" + File.separator + v + File.separator + accountId + ".json");
    }

    private static File fileUserNameTxt(long accountId) {
        return new File(Main.pathHome() + "cache" + File.separator + "name" + File.separator + accountId + ".txt");
    }

    /**
     * 是否创建了文件
     *
     * @param file
     * @return
     */
    private static boolean fileCreateCheck(File file) {
        int i = file.getPath().lastIndexOf(File.separator);
        String substring = file.getPath().substring(0, i);
        var f = new File(substring);
        if (!f.exists()) {
            return f.mkdirs();
        }
        return false;
    }
}
