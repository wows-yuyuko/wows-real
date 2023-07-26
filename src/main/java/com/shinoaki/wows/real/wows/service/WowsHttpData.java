package com.shinoaki.wows.real.wows.service;

import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.real.config.ProxyConfig;
import com.shinoaki.wows.real.config.type.DataType;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.util.Objects;

/**
 * @author Xun
 * create or update time = 2023/7/26 18:18 星期三
 */
public class WowsHttpData {
    private final HttpClient httpClient;
    private final ProxyConfig config;
    private final WowsServer server;
    private final DataType dataType;

    public WowsHttpData(ProxyConfig config, WowsServer server) {
        HttpClient.Builder builder = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS);
        ProxyConfig.ProxyServer proxyServer = config.findServer(server);
        if (proxyServer != null && proxyServer.enableProxy()) {
            this.httpClient = builder.proxy(ProxySelector.of(new InetSocketAddress(config.host(), config.port()))).build();
        } else {
            this.httpClient = builder.build();
        }

        this.dataType = Objects.requireNonNull(config.findServer(server)).dataType();
        this.config = config;
        this.server = server;
    }

    public WowsServer server() {
        return server;
    }

    public ProxyConfig config() {
        return config;
    }

    public HttpClient httpClient() {
        return httpClient;
    }

    public DataType dataType() {
        return dataType;
    }
}
