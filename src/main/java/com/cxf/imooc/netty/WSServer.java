package com.cxf.imooc.netty;

import com.cxf.imooc.netty.initializer.HelloServerInitializer;
import com.cxf.imooc.netty.initializer.WSServerInitializer;
import com.cxf.imooc.service.LocationRedisService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/23 0:11
 * @description：实现客户端发送一个请求，服务器会返回hello netty
 * @version:
 */
@Component
public class WSServer {

    @Value("${webSocket.port}")
    private Integer port;

    private static Logger logger = LoggerFactory.getLogger(WSServer.class);
    private EventLoopGroup bossGruop ; //主线程组，用于接收客户端的连接，将操作交给从线程组，不做具体处理
    private EventLoopGroup workerGruop ; //从线程组，用于处理住线程组交过来的操作
    private  ChannelFuture channelFuture;
    private static ChannelGroup channelGroup ;
    private ServerBootstrap serverBootstrap;  //netty服务器 创建，使用ServerBootstrap启动类

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private WSServerInitializer wsServerInitializer;

    @Autowired
    private LocationRedisService locationRedisService;

    public WSServer(){
        //定义一对线程组
        bossGruop = new NioEventLoopGroup();
        workerGruop = new NioEventLoopGroup();

        serverBootstrap = new ServerBootstrap();
        channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    }

    public void init(){
        wsServerInitializer.setGroup(channelGroup);
        serverBootstrap.group(bossGruop,workerGruop)
                .channel(NioServerSocketChannel.class)  //设置通道类型
                .childHandler(wsServerInitializer);//设置初始化器

        TimerTask task = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (channelGroup.size() != 0){
                    String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);

                    logger.info("[{}]---run service timer----",dateTime);

                    String locations = locationRedisService.getMachineRealTimeLocationsJson();
                    String msgToClient = String.format("[%s]: %s",dateTime,locations);
                    channelGroup.writeAndFlush(new TextWebSocketFrame(msgToClient));
                }

                //任务执行完成后再把自己添加到任务solt上
                MyTimer.addTask(this);
            }
        };
        MyTimer.addTask(task);
    }


    public void start(Integer port){
        //启动server 设置8088为启动端口号
        this.channelFuture = serverBootstrap.bind(port);
        if (channelFuture!=null){
            logger.info("netty启动完成,port: {}",port);
        }

    }

    public void start(){
        start(this.port);

    }



}
