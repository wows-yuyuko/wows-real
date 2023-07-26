package com.shinoaki.wows.real.wows.user;

import com.shinoaki.wows.api.type.WowsServer;

/**
 * @author Xun
 * @date 2023/5/14 15:21 星期日
 */
public record WowsUserInfo(

        String server,

        String serverCn,

        WowsClanInfo clanInfo,

        long accountId,

        String userName,

        long accountCreateTime) {

    public static WowsUserInfo user(long accountId, String userName, WowsServer server, WowsClanInfo clanInfo, long createTime) {
        return new WowsUserInfo(server.getCode(), server.getName(), clanInfo, accountId, userName, createTime);
    }

    public static WowsUserInfo empty() {
        return new WowsUserInfo("?", "?", WowsClanInfo.empty(), 0, "?", 0);
    }
}
