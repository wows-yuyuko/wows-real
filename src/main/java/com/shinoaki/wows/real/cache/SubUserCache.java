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

    private static final ConcurrentMap<ChannelId, Set<AccountInfo>> USER_CACHE = new ConcurrentHashMap<>();

    public static void put(ChannelId id, Set<AccountInfo> accountId) {
        log.info("{} 添加账号信息={}", id, accountId);
        USER_CACHE.put(id, accountId);
    }


    public static Map<ChannelId, AccountInfo> check(long accountId) {
        Map<ChannelId, AccountInfo> map = new HashMap<>();
        for (var kv : USER_CACHE.entrySet()) {
            Optional<AccountInfo> first = kv.getValue().stream().filter(x -> x.accountId() == accountId).findFirst();
            first.ifPresent(aLong -> map.put(kv.getKey(), aLong));
        }
        return map;
    }

    public static Map<WowsServer, List<AccountInfo>> all() {
        Set<AccountInfo> set = new HashSet<>();
        USER_CACHE.values().forEach(set::addAll);
        return set.stream().collect(Collectors.groupingBy(AccountInfo::server));
    }

    public static List<Long> allAccountId() {
        Set<AccountInfo> set = new HashSet<>();
        USER_CACHE.values().forEach(set::addAll);
        return set.stream().map(AccountInfo::accountId).toList();
    }


}
