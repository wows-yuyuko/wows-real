package com.shinoaki.wows.real.wows;


import com.fasterxml.jackson.core.type.TypeReference;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.pr.PrData;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.real.wows.global.NationData;
import com.shinoaki.wows.real.wows.global.PrInfo;
import com.shinoaki.wows.real.wows.global.ShipType;
import com.shinoaki.wows.real.wows.source.DamageData;
import com.shinoaki.wows.real.wows.source.WinsData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author Xun
 * @date 2023/5/11 23:54 星期四
 */
public class WowsCache {
    private WowsCache() {

    }

    private static final List<PrInfo> PR_INFO_LIST = new ArrayList<>();
    private static final List<NationData> NATION_DATA_LIST = new ArrayList<>();
    private static final List<ShipType> SHIP_TYPE_LIST = new ArrayList<>();
    private static final Map<String, List<DamageData>> COLOR_DAMAGE_LIST = new HashMap<>();
    private static final List<WinsData> COLOR_WINS_LIST = new ArrayList<>();
    private static final Map<Long, WowsShipInfo> SHIP_MAP = new TreeMap<>();
    private static final Map<Long, PrData> SHIP_PR_MAP = new TreeMap<>();
    private static final Set<Integer> LEVEL = new TreeSet<>();
    private static final Set<String> GROUP_TYPE = new HashSet<>();
    private static final Set<String> SHIP_TYPE = new HashSet<>();
    private static final Set<String> COUNTRY = new HashSet<>();

    public static List<PrInfo> getPrInfoList() {
        return PR_INFO_LIST;
    }

    public static List<ShipType> getShipTypeList() {
        return SHIP_TYPE_LIST;
    }

    public static List<NationData> getNationDataList() {
        return NATION_DATA_LIST;
    }

    public static WowsShipInfo getShipMap(long shipId) {
        return SHIP_MAP.getOrDefault(shipId, WowsShipInfo.empty(shipId));
    }

    public static Map<Long, WowsShipInfo> getShipMap() {
        return SHIP_MAP;
    }

    public static List<String> groupType() {
        return GROUP_TYPE.stream().toList();
    }

    public static List<String> shipType() {
        return SHIP_TYPE.stream().toList();
    }

    public static List<Integer> level() {
        return LEVEL.stream().toList();
    }

    public static List<String> country() {
        return COUNTRY.stream().toList();
    }


    public static void init(List<WowsShipInfo> list, Map<Long, PrData> prDataMap, String path) throws IOException {
        JsonUtils utils = new JsonUtils();
        loadPr(new File(path + "Pr.json"), utils);
        loadShipType(new File(path + "ShipType.json"), utils);
        loadNation(new File(path + "Nation.json"), utils);
        loadDamage(new File(path + "Damage.json"), utils);
        loadWins(new File(path + "Wins.json"), utils);
        if (!list.isEmpty()) {
            list.forEach(x -> {
                SHIP_MAP.put(x.shipId(), x);
                LEVEL.add(x.level());
                GROUP_TYPE.add(x.groupType());
                SHIP_TYPE.add(x.shipType());
                COUNTRY.add(x.country());
            });
        }
        if (!prDataMap.isEmpty()) {
            SHIP_PR_MAP.clear();
            SHIP_PR_MAP.putAll(prDataMap);
        }
    }

    public static PrData getPr(long pr) {
        return SHIP_PR_MAP.getOrDefault(pr, PrData.empty());
    }

    public static PrInfo getPr(int pr) {
        if (pr <= 0) {
            return PR_INFO_LIST.get(0);
        }
        for (var x : PR_INFO_LIST) {
            if (pr < x.value()) {
                return x;
            }
        }
        return PR_INFO_LIST.get(PR_INFO_LIST.size() - 1);
    }

    public static DamageData getDamage(long shipId, double value) {
        List<DamageData> list = COLOR_DAMAGE_LIST.getOrDefault(WowsCache.getShipMap(shipId).shipType(), List.of());
        if (list.isEmpty()) {
            return DamageData.empty();
        }
        if (value <= 0) {
            return list.get(0);
        }
        for (var x : list) {
            if (value < x.value()) {
                return x;
            }
        }
        return list.get(list.size() - 1);
    }

    public static WinsData getWins(double value) {
        if (value <= 0.0) {
            return COLOR_WINS_LIST.get(0);
        }
        for (var x : COLOR_WINS_LIST) {
            if (value < x.value()) {
                return x;
            }
        }
        return COLOR_WINS_LIST.get(COLOR_WINS_LIST.size() - 1);
    }

    /**
     * 双精度溢出验证-外加小数点后两位
     *
     * @param data 双精度数值
     * @return NaN等全部返回0
     */
    public static double doubleCheckAnd_HALF_UP(double data) {
        if (Double.isInfinite(data)) {
            return 0.0;
        } else if (Double.isNaN(data)) {
            return 0.0;
        } else if (data <= 0.0) {
            return 0.0;
        } else {
            return BigDecimal.valueOf(data).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
    }

    public static int doubleCheckAnd_HALF_UP_Int(double data) {
        if (Double.isInfinite(data)) {
            return 0;
        } else if (Double.isNaN(data)) {
            return 0;
        } else if (data <= 0.0) {
            return 0;
        } else {
            return BigDecimal.valueOf(data).setScale(0, RoundingMode.HALF_UP).intValue();
        }
    }

    public static Map<Long, Map<WowsBattlesType, ShipInfo>> process(Map<WowsBattlesType, List<ShipInfo>> info, long shipId) {
        Map<Long, Map<WowsBattlesType, ShipInfo>> shipMap = new TreeMap<>();
        if (shipId == 0) {
            for (var entry : info.entrySet()) {
                for (var value : entry.getValue()) {
                    Map<WowsBattlesType, ShipInfo> shipInfoMap = shipMap.getOrDefault(value.shipId(), new EnumMap<>(WowsBattlesType.class));
                    shipInfoMap.put(entry.getKey(), value);
                    shipMap.put(value.shipId(), shipInfoMap);
                }
            }
        } else {
            for (var entry : info.entrySet()) {
                for (var value : entry.getValue()) {
                    if (value.shipId() == shipId) {
                        Map<WowsBattlesType, ShipInfo> shipInfoMap = shipMap.getOrDefault(value.shipId(), new EnumMap<>(WowsBattlesType.class));
                        shipInfoMap.put(entry.getKey(), value);
                        shipMap.put(value.shipId(), shipInfoMap);
                    }
                }
            }
        }
        return shipMap;
    }

    public static int zero(int data) {
        return Math.max(data, -1);
    }

    public static long zero(long data) {
        return Math.max(data, -1);
    }

    private static void loadPr(File file, JsonUtils utils) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            loadPrInfo(utils.parse(new String(in.readAllBytes()), new TypeReference<List<PrInfo>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
    }

    private static void loadDamage(File file, JsonUtils utils) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            loadDamage(utils.parse(new String(in.readAllBytes()), new TypeReference<Map<String, List<DamageData>>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
    }

    private static void loadWins(File file, JsonUtils utils) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            loadWins(utils.parse(new String(in.readAllBytes()), new TypeReference<List<WinsData>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
    }

    private static void loadNation(File file, JsonUtils utils) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            loadNation(utils.parse(new String(in.readAllBytes()), new TypeReference<List<NationData>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
    }

    private static void loadShipType(File file, JsonUtils utils) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            loadShipType(utils.parse(new String(in.readAllBytes()), new TypeReference<List<ShipType>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
    }

    private static void loadPrInfo(List<PrInfo> info) {
        PR_INFO_LIST.clear();
        PR_INFO_LIST.addAll(info);
    }

    private static void loadDamage(Map<String, List<DamageData>> info) {
        COLOR_DAMAGE_LIST.clear();
        COLOR_DAMAGE_LIST.putAll(info);
    }

    private static void loadWins(List<WinsData> info) {
        COLOR_WINS_LIST.clear();
        COLOR_WINS_LIST.addAll(info);
    }

    private static void loadNation(List<NationData> info) {
        NATION_DATA_LIST.clear();
        NATION_DATA_LIST.addAll(info);
    }

    private static void loadShipType(List<ShipType> info) {
        SHIP_TYPE_LIST.clear();
        SHIP_TYPE_LIST.addAll(info);
    }

    private static boolean isEmptyOrNull(String data) {
        return data == null || data.isBlank();
    }

    private static boolean matchName(String shipName, String matchName, String matchNameEnglish) {
        if (isEmptyOrNull(shipName)) {
            return true;
        }
        String u = shipName.toLowerCase(Locale.ROOT);
        return matchName.toLowerCase(Locale.ROOT).contains(u) || matchNameEnglish.toLowerCase(Locale.ROOT).contains(u);
    }
}
