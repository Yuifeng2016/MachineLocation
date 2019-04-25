package com.cxf.imooc.netty;

import com.cxf.imooc.netty.initializer.HelloServerInitializer;
import com.cxf.imooc.netty.initializer.WSServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    private ServerBootstrap serverBootstrap;  //netty服务器 创建，使用ServerBootstrap启动类

    @Autowired
    private WSServerInitializer wsServerInitializer;
//    private static class SingletionWSServer {
//        static final WSServer INSTANCE = new WSServer();
//    }
//
//    public static WSServer getInstance(){
//        return SingletionWSServer.INSTANCE;
//    }

    public WSServer(){
        //定义一对线程组
        bossGruop = new NioEventLoopGroup();
        workerGruop = new NioEventLoopGroup();

        serverBootstrap = new ServerBootstrap();

    }

    public void init(){
        serverBootstrap.group(bossGruop,workerGruop)
                .channel(NioServerSocketChannel.class)  //设置通道类型
                .childHandler(wsServerInitializer);//设置初始化器
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
