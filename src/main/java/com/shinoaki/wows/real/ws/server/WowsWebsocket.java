package com.shinoaki.wows.real.ws.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Xun
 * create or update time = 2023/7/25 19:21 星期二
 */
public class WowsWebsocket {
    private final Logger log = LoggerFactory.getLogger(WowsWebsocket.class);
    private final EventLoopGroup serverGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public void start(int port, String path) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(serverGroup, workerGroup).channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new WowsWebSocketServerInitializer(path));
        bootstrap.bind(port).sync();
        log.info("websocket服务器启动! local port={} path={}", port, path);
    }

    public void close() {
        serverGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
