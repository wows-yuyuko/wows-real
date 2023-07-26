package com.shinoaki.wows.real.ws.server;


import com.shinoaki.wows.real.ws.server.handler.ChannelEventsHandler;
import com.shinoaki.wows.real.ws.server.handler.WebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;

import java.util.List;

/**
 * @author Xun
 * create or update time = 2023/7/25 16:41 星期二
 */
public class WowsWebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslContext;
    private final int maxContentLength;
    private final List<String> websocketPathList;

    public WowsWebSocketServerInitializer(String websocketPath) {
        this.sslContext = null;
        this.maxContentLength = 65536;
        if (websocketPath.startsWith("/")) {
            this.websocketPathList = List.of(websocketPath);
        } else {
            this.websocketPathList = List.of("/" + websocketPath);
        }
    }


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //ssl处理
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new ChannelEventsHandler(this.websocketPathList));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(maxContentLength));
        this.websocketPathList.forEach(path -> pipeline.addLast(new WebSocketServerProtocolHandler(WebSocketServerProtocolConfig.newBuilder().websocketPath(path).subprotocols(null).allowExtensions(true).build())));
        pipeline.addLast(new WebSocketFrameHandler());
    }
}
