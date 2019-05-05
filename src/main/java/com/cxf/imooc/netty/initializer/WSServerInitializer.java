package com.cxf.imooc.netty.initializer;


import com.cxf.imooc.entity.MachineRealTimeLocation;
import com.cxf.imooc.netty.MyTimer;
import com.cxf.imooc.netty.OnlineContainer;
import com.cxf.imooc.netty.handler.OnlineMachineHandler;
import com.cxf.imooc.netty.handler.TextWebSocketHandler;
import com.cxf.imooc.service.LocationRedisService;
import com.cxf.imooc.util.BeansUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/23 20:38
 * @description：WSServer初始化类
 * @version:
 */
@Component
public class WSServerInitializer extends ChannelInitializer<SocketChannel> {
    private final int MAX_CONTENT_LENGTH = 1024*64;
    private Logger logger = LoggerFactory.getLogger(this.getClass()); // 日志对象

    private static ChannelGroup group ;

    @Autowired
    private OnlineContainer onlineContainer;

    public WSServerInitializer (){

        initTask();
    }



    @Autowired
    private LocationRedisService locationRedisService;


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //创建管道
        ChannelPipeline pipeline = ch.pipeline();

        //HttpRequestDecoder和HttpResponseEncoder的一个组合，针对http协议进行编解码
        pipeline.addLast(new LoggingHandler(LogLevel.TRACE));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new StringDecoder());
        //分块向客户端写数据，防止发送大文件时导致内存溢出， channel.write(new ChunkedFile(new File("bigFile.mkv")))
        pipeline.addLast(new ChunkedWriteHandler());
        // 对http message进行聚合，有FullHttpResponse，FullHttpRequest
        // 需要放到HttpServerCodec这个处理器后面
        pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        //===============以上用于支持http协议==============

        pipeline.addLast(new TextWebSocketHandler());
        //自定义处理器1

        /**
         * websocket 服务器处理的协议，用于制定给客户端访问的路由地址
         * 本handler会帮你处理一些复杂的事情
         * 比如握手的动作(close,ping,pong)
         * 对于websocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        //pipeline.addLast(new OnlineMachineHandler(locationRedisService,group));


    }

    public void setGroup(ChannelGroup group) {
        this.group = group;
    }

    public void initTask(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TimerTask task = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (onlineContainer.getOnlineUserMap().size() != 0){
                    String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);


                    onlineContainer.getUserMap().forEach((username,ctxId)->{
                        String locations = locationRedisService.getMachineRealTimeLocationsJson(username);
                        String msgToClient = String.format("[%s]: %s",dateTime,locations);
                        //logger.info("username:{},locations:{}",username,locations);
                        ChannelHandlerContext context = onlineContainer.getChannelHandlerContextByUserId(username);
                        context.channel().writeAndFlush(new TextWebSocketFrame(msgToClient));
                    });
                }

                //任务执行完成后再把自己添加到任务solt上
                MyTimer.addTask(this);
            }
        };
        MyTimer.addTask(task);
    }
}
