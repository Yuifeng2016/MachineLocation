package com.cxf.imooc.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/23 20:31
 * @description：netty启动类
 * @version:
 */
@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {

//    @Value("${webSocket.port}")
//    private Integer port;

    @Autowired
    WSServer wsServer;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null){
            try {
                //WSServer.getInstance().start(port);
                wsServer.init();
                wsServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
