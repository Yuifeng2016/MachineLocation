package com.cxf.imooc.netty.initializer;

import com.cxf.imooc.netty.handler.CustomHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/23 0:33
 * @description：channel初始化器
 * @version:
 */
public class HelloServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //通过channel去获取对应的管道
        ChannelPipeline pipeline = channel.pipeline();
        //通过管道添加handler
        //HttpServerCodec是netty的助手类，对请求进行编解码
        pipeline.addLast("HttpServerCodec",new HttpServerCodec());
        pipeline.addLast("CustomHandler",new CustomHandler());
    }
}
