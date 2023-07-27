package com.shinoaki.wows.real.cache;

import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.real.wows.AccountInfo;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author Xun
 * create or update time = 2023/7/25 16:51 星期二
 */
public class SubUserCache {
    private static final Logger log = LoggerFactory.getLogger(SubUserCache.class);

    private SubUserCache() {
    }

    private static final ConcurrentMap<ChannelId, Set<AccountInfo>> WS_ID = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Set<AccountInfo>> HTTP_ID = new ConcurrentHashMap<>();

    public static void put(ChannelId id, Set<AccountInfo> accountId) {
        log.info("{} 添加账号信息={}", id, accountId);
        WS_ID.put(id, accountId);
    }

    public static void put(String id, Set<AccountInfo> accountId) {
        log.info("{} 添加账号信息={}", id, accountId);
        HTTP_ID.put(id, accountId);
    }


    public static Map<ChannelId, AccountInfo> checkWs(long accountId) {
        Map<ChannelId, AccountInfo> map = new HashMap<>();
        for (var kv : WS_ID.entrySet()) {
            Optional<AccountInfo> first = kv.getValue().stream().filter(x -> x.accountId() == accountId).findFirst();
            first.ifPresent(aLong -> map.put(kv.getKey(), aLong));
        }
        return map;
    }

    public static Map<String, AccountInfo> checkHttp(long accountId) {
        Map<String, AccountInfo> map = new HashMap<>();
        for (var kv : HTTP_ID.entrySet()) {
            Optional<AccountInfo> first = kv.getValue().stream().filter(x -> x.accountId() == accountId).findFirst();
            first.ifPresent(aLong -> map.put(kv.getKey(), aLong));
        }
        return map;
    }

    public static Map<WowsServer, List<AccountInfo>> all() {
        Set<AccountInfo> set = new HashSet<>();
        WS_ID.values().forEach(set::addAll);
        HTTP_ID.values().forEach(set::addAll);
        return set.stream().collect(Collectors.groupingBy(AccountInfo::server));
    }

    public static List<Long> allAccountId() {
        Set<AccountInfo> set = new HashSet<>();
        WS_ID.values().forEach(set::addAll);
        HTTP_ID.values().forEach(set::addAll);
        return set.stream().map(AccountInfo::accountId).toList();
    }


}
