package com.shinoaki.wows.real.ws.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Xun
 * create or update time = 2023/7/25 16:55 星期二
 */
public class ChannelEventsHandler extends ChannelDuplexHandler {
    private final Logger log = LoggerFactory.getLogger(ChannelEventsHandler.class);
    private static final ConcurrentMap<ChannelId, Channel> CHANNEL_ID_CHANNEL_CONCURRENT_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> BOOLEAN_MAP = new HashMap<>();

    public ChannelEventsHandler(List<String> path) {
        path.forEach(x -> BOOLEAN_MAP.put(x, Boolean.TRUE));
    }

    private void put(Channel channel) {
        CHANNEL_ID_CHANNEL_CONCURRENT_MAP.put(channel.id(), channel);
    }

    private void remove(ChannelId id) {
        CHANNEL_ID_CHANNEL_CONCURRENT_MAP.remove(id);
    }

    public boolean check(String path) {
        return BOOLEAN_MAP.getOrDefault(path, false);
    }

    public static void sendMsg(ChannelId id, String msg) {
        Channel channel = CHANNEL_ID_CHANNEL_CONCURRENT_MAP.getOrDefault(id, null);
        if (channel != null) {
            channel.writeAndFlush(new TextWebSocketFrame(msg));
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channelRegistered 用户建立连接={}", ctx.channel().id());
        this.put(ctx.channel());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channelUnregistered 用户断开连接={}", ctx.channel().id());
        this.remove(ctx.channel().id());
        super.channelUnregistered(ctx);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpObject httpObject && httpObject instanceof HttpRequest request) {
            String uri = request.uri();
            if (!check(uri)) {
                log.info("未匹配的uri={}", uri);
                ctx.channel().close();
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }
}
