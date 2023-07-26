package com.shinoaki.wows.real.timer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.pr.PrData;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.real.Main;
import com.shinoaki.wows.real.cache.SubUserCache;
import com.shinoaki.wows.real.mqtt.MqttService;
import com.shinoaki.wows.real.service.WowsUserService;
import com.shinoaki.wows.real.service.WsService;
import com.shinoaki.wows.real.wows.WowsCache;
import com.shinoaki.wows.real.wows.WowsShipInfo;
import com.shinoaki.wows.real.wows.service.WowsHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Xun
 * create or update time = 2023/7/25 20:56 星期二
 */
public class ServerTimer {
    public static final Logger log = LoggerFactory.getLogger(ServerTimer.class);
    public static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    public void updateConfig() {
        EXECUTOR_SERVICE.scheduleWithFixedDelay(() -> {
            try {
                JsonUtils json = new JsonUtils();
                WowsCache.init(shipList(json), shipPr(json), Main.pathHome() + "config" + File.separator);
            } catch (IOException e) {
                log.error("加载wows cache缓存异常!", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("加载wows cache缓存异常!", e);
            }
        }, 0, 6, TimeUnit.HOURS);
    }

    public void timerQueryAccountId() {
        EXECUTOR_SERVICE.scheduleWithFixedDelay(() -> {
            for (var kv : SubUserCache.all().entrySet()) {
                WowsHttpData wowsHttpData = WowsUserService.http(kv.getKey());
                kv.getValue().forEach(x -> WsService.userPlayerOnShip(wowsHttpData, x.accountId()));
            }
        }, 1, 5, TimeUnit.MINUTES);
    }

    public void timerSubTopic(MqttService service) {
        List<Long> longs = SubUserCache.allAccountId();
        if (longs.isEmpty()) {
            EXECUTOR_SERVICE.scheduleWithFixedDelay(service::onMessage, 10, 60, TimeUnit.SECONDS);
        } else {
            EXECUTOR_SERVICE.scheduleWithFixedDelay(service::onMessage, 1, 5, TimeUnit.MINUTES);
        }
    }


    private static List<WowsShipInfo> shipList(JsonUtils json) throws IOException, InterruptedException {
        final URI uri = URI.create("https://v3-api.wows.shinoaki.com/public/wows/encyclopedia/ship/search");
        HttpClient client = HttpClient.newBuilder().build();
        HttpResponse<byte[]> response = client.send(HttpRequest.newBuilder().GET().uri(uri).setHeader("Yuyuko-Client-Type", "SWAGGER;test").build(),
                HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 200) {
            JsonNode node = json.parse(new String(response.body()));
            return json.parse(node.get("data").toString(), new TypeReference<List<WowsShipInfo>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
        }
        log.error("请求战舰信息数据异常!");
        return List.of();
    }

    private static Map<Long, PrData> shipPr(JsonUtils json) throws IOException, InterruptedException {
        final URI uri = URI.create("https://v3-api.wows.shinoaki.com/public/wows/encyclopedia/ship/server/data");
        HttpClient client = HttpClient.newBuilder().build();
        HttpResponse<byte[]> response = client.send(HttpRequest.newBuilder().GET().uri(uri).setHeader("Yuyuko-Client-Type", "SWAGGER;test").build(),
                HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 200) {
            JsonNode node = json.parse(new String(response.body()));
            return json.parse(node.get("data").toString(), new TypeReference<Map<Long, PrData>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
        }
        log.error("请求战舰pr信息数据异常!");
        return Map.of();
    }
}
