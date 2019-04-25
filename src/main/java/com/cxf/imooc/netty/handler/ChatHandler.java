package com.cxf.imooc.netty.handler;

import com.cxf.imooc.entity.MachineRealTimeLocation;
import com.cxf.imooc.entity.User;
import com.cxf.imooc.netty.WSServer;
import com.cxf.imooc.util.CommonUtil;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/23 20:36
 * @description：处理消息的助手类
 * TextWebSocketFrame：在netty中，是用于为websocket专门处理文本的对象，frame是消息的载体
 * @version:
 */

public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static Logger logger = LoggerFactory.getLogger(ChatHandler.class);

    private static final int EXPIRE_TIME = 1 ;

    private RedisTemplate<String, Serializable> redisCacheTemplate;

    public void setRedisCacheTemplate(RedisTemplate<String, Serializable> redisCacheTemplate) {
        this.redisCacheTemplate = redisCacheTemplate;
    }

    //用于记录和管理所有客户端的channel
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    public ChatHandler() {

    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        // 1.获取上传的消息
        String text = msg.text();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);
        if (text.isEmpty()){
            ctx.channel().writeAndFlush(new TextWebSocketFrame("["+dateTime +"]:"+"内容不能为空"));
            return;
        }
        logger.info("客户端传过来的消息:{}",text);

        // 2.尝试转换为MachineRealTimeLocation对象
        ObjectMapper mapper  = new ObjectMapper();
        MachineRealTimeLocation location = null;
        try {
            location = mapper.readValue(text, MachineRealTimeLocation.class);
            boolean fieldIsNull = CommonUtil.checkObjFieldIsNull(location,"taskId","type");
            if(fieldIsNull){
                ctx.channel().writeAndFlush(new TextWebSocketFrame("["+dateTime +"]:"+"json参数不完整"));
                return;
            }
        } catch (IOException e) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("["+dateTime +"]:"+"json转换异常"));
            return;
        }

        // 3.获取过期时间
        Long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        Long oldSecond = nowSecond - EXPIRE_TIME * 60; //分钟

        String key = "south:machine:Location";
        redisCacheTemplate.opsForZSet().add(key,location,nowSecond);

        Set<Serializable> activeLocations = redisCacheTemplate.opsForZSet().rangeByScore(key, oldSecond, nowSecond);
        List<Serializable> list =  new ArrayList<>(activeLocations);
        String msgToClient = "activeMachine：" +mapper.writeValueAsString(list);
        //logger.info("5分钟内更新的位置数：{}",activeLocations.size());

        //把消息刷到所有客户端
        clients.writeAndFlush(new TextWebSocketFrame("["+dateTime +"]:"+msgToClient));
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
