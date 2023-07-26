package com.shinoaki.wows.real.ws.server.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.real.service.WsService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Xun
 * create or update time = 2023/7/25 16:32 星期二
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame msg) {
            JsonUtils json = new JsonUtils();
            JsonNode node = json.parse(msg.text());
            WsService.select(ctx.channel().id(), node.get("path").asText(), node.get("data").toString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("websocket异常!", cause);
    }

}
