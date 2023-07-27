package com.shinoaki.wows.real.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.real.cache.SubUserCache;
import com.shinoaki.wows.real.wows.AccountInfo;
import com.shinoaki.wows.real.wows.view.UserShipInfoView;
import com.shinoaki.wows.real.ws.data.BaseWsPackage;
import com.shinoaki.wows.real.ws.data.WsPathType;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Xun
 * create or update time = 2023/7/27 16:20 星期四
 */
public class HttpService {
    private final HttpServer server;
    private static final Logger log = LoggerFactory.getLogger(HttpService.class);

    private static final ConcurrentMap<String, Queue<UserShipInfoView>> QUEUE_MAP = new ConcurrentHashMap<>();

    public static void put(String botId, UserShipInfoView view) {
        Queue<UserShipInfoView> queue = QUEUE_MAP.getOrDefault(botId, new ArrayBlockingQueue<>(1000));
        queue.add(view);
        QUEUE_MAP.put(botId, queue);
    }

    public static UserShipInfoView pool(String botId) {
        Queue<UserShipInfoView> queue = QUEUE_MAP.get(botId);
        if (queue != null) {
            return queue.poll();
        }
        return null;
    }

    public static void queueUserPlayOne(long accountId, UserShipInfoView view) {
        for (var key : SubUserCache.checkHttp(accountId).keySet()) {
            log.info("{}-{} http推送消息... {}", key, accountId, WsPathType.user_real_info);
            put(key, view);
        }
    }


    public HttpService(InetSocketAddress addr) throws IOException {
        this.server = HttpServer.create(addr, 0);
    }

    public void start() {
        post_user_accountId();
        get_user_real_info();
        server.start();
    }

    private void post_user_accountId() {
        server.createContext("/" + WsPathType.user_accountId.name(), exchange -> {
            String method = exchange.getRequestMethod();
            if ("post".equalsIgnoreCase(method)) {
                JsonUtils json = new JsonUtils();
                try (InputStream inputStream = exchange.getRequestBody();) {
                    String data = new String(inputStream.readAllBytes());
                    SubUserCache.put(getBotId(exchange), json.parse(data, new TypeReference<Set<AccountInfo>>() {
                        @Override
                        public Type getType() {
                            return super.getType();
                        }
                    }));
                    response(exchange, new BaseWsPackage<>(200, WsPathType.user_accountId, ""));
                } catch (Exception e) {
                    log.info("序列化数据异常!", e);
                }
            }
            response(exchange, new BaseWsPackage<>(500, WsPathType.user_accountId, "类型错误"));
        });
    }

    private void get_user_real_info() {
        server.createContext("/" + WsPathType.user_real_info.name(), exchange -> {
            String botId = getBotId(exchange);
            UserShipInfoView view = pool(botId);
            if (view != null && ((Instant.now().toEpochMilli() - view.battleInfo().recordTime()) <= 60000)) {
                    response(exchange, new BaseWsPackage<>(WsPathType.user_real_info, view));

            }
            response(exchange, new BaseWsPackage<>(404, WsPathType.user_real_info, ""));
        });
    }

    private void response(HttpExchange exchange, BaseWsPackage<?> baseWsPackage) throws IOException {
        JsonUtils json = new JsonUtils();
        byte[] data = json.toJson(baseWsPackage).getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(200, data.length);
        try (var out = exchange.getResponseBody()) {
            out.write(data);
            out.flush();
        }
        exchange.close();
    }

    private static String getBotId(HttpExchange exchange) {
        String[] query = exchange.getRequestURI().getQuery().split("&");
        for (var s : query) {
            if (s.startsWith("botId=")) {
                return s.substring(6);
            }
        }
        return "";
    }
}
