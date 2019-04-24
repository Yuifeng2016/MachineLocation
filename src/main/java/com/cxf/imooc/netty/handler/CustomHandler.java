package com.cxf.imooc.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/23 0:40
 * @description：自定义助手类
 * SimpleChannelInboundHandler相当于入站，入境
 * @version:
 */
public class CustomHandler extends SimpleChannelInboundHandler<HttpObject> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        //获取channel
        Channel channel = ctx.channel();
        //显示客户端 远程地址
        System.out.println(channel.remoteAddress());
        //定义要发送的消息
        ByteBuf content = Unpooled.copiedBuffer("Hello Netty",CharsetUtil.UTF_8);
        //构建响应发送到客户端
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                content);
        //设置响应内容
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,content.readableBytes());
        //把响应发回客户端
        ctx.writeAndFlush(response);
    }
}
