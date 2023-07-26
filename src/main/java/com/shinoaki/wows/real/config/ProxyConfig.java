package com.shinoaki.wows.real.config;

import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.real.config.type.DataType;

/**
 * @author Xun
 * create or update time = 2023/7/20 11:53 星期四
 */
public record ProxyConfig(String host, int port, ProxyServer[] server) {

    public ProxyServer findServer(WowsServer server) {
        for (var s : server()) {
            if (s.wowsServer() == server) {
                return s;
            }
        }
        return null;
    }

    public record ProxyServer(boolean enableProxy, String server, String type) {

        public WowsServer wowsServer() {
            return WowsServer.findCodeByNull(server);
        }

        public DataType dataType() {
            if (DataType.API.name().equalsIgnoreCase(type)) {
                return DataType.API;
            } else if (DataType.VORTEX.name().equalsIgnoreCase(type)) {
                return DataType.VORTEX;
            } else {
                return null;
            }
        }
    }
}
