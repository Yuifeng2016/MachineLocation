package com.cxf.imooc.netty.initializer;

import com.cxf.imooc.netty.handler.ChatHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/23 20:38
 * @description：WSServer初始化类
 * @version:
 */
public class WSServerInitializer extends ChannelInitializer<SocketChannel> {
    private final int MAX_CONTENT_LENGTH = 1024*64;

    private static ChannelGroup group;
    public WSServerInitializer (){

    }

    public WSServerInitializer (ChannelGroup group){
        this.group = group;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //创建管道
        ChannelPipeline pipeline = ch.pipeline();

        //websocket 基于http协议，所以要有http编解码器
        pipeline.addLast(new HttpServerCodec());
        // 对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());
        // 对http message进行聚合，有FullHttpResponse，FullHttpRequest
        // 肌肤在netty中的编程都会用到此handler
        pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        //===============以上用于支持http协议==============

        /**
         * websocket 服务器处理的协议，用于制定给客户端访问的路由地址
         * 本handler会帮你处理一些复杂的事情
         * 比如握手的动作(close,ping,pong)
         * 对于websocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //自定义助手类
        pipeline.addLast(new ChatHandler());
    }
}