package com.cxf.imooc.netty.initializer;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/24 21:59
 * @description：加密初始化类
 * @version:
 */
public class SecureChatServerInitializer extends WSServerInitializer {
    private final SslContext context;
    public SecureChatServerInitializer(ChannelGroup group,
                                       SslContext context) {
        super(group);
        this.context = context;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        super.initChannel(ch);
        SSLEngine engine = context.newEngine(ch.alloc());
        ch.pipeline().addFirst(new SslHandler(engine));
    }
}
