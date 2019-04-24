package com.cxf.imooc.netty.handler;

import com.cxf.imooc.entity.User;
import com.cxf.imooc.netty.WSServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/23 20:36
 * @description：处理消息的助手类
 * TextWebSocketFrame：在netty中，是用于为websocket专门处理文本的对象，frame是消息的载体
 * @version:
 */

public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static Logger logger = LoggerFactory.getLogger(ChatHandler.class);

    @Autowired
    private RedisTemplate<String, Serializable> redisCacheTemplate;

    //用于记录和管理所有客户端的channel
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static ChannelGroup group;

    public ChatHandler() {

    }
    public ChatHandler(ChannelGroup group) {
        this.group = group;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        logger.info("客户端传过来的消息:{}",text);
        ObjectMapper mapper  = new ObjectMapper();
//        User user = new User(1L, "老王", "pa");
//        String userJson = mapper.writeValueAsString(user);
        User user = mapper.readValue(text, User.class);

        //把消息保存到数据库
        String key = "battcn:user:"+user.getId();
        redisCacheTemplate.opsForValue().set(key, user);


        //把消息刷到所有客户端


        //clients.writeAndFlush(new TextWebSocketFrame("[服务器在 "+LocalDateTime.now() + " 接收到消息]:"+text));
    }

    /**
     * 当客户端连接服务端后，获取客户端的channel，并放到channelGroup中进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("channel added：{}",ctx.channel().id().asShortText());
        clients.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //当触发handlerRemoved，channel会自动移除对应客户端的channel
//        clients.remove(ctx.channel());
        //logger.info("channel long id：{}",ctx.channel().id().asLongText());
        logger.info("channel removed ,short id：{}",ctx.channel().id().asShortText());
    }
}
