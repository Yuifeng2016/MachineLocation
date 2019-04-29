package com.cxf.imooc.netty;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/26 17:41
 * @description：定时从redis中读取数据发送
 */
@Configuration
public class MyTimer {
    private static final HashedWheelTimer timer =
            new HashedWheelTimer(Executors.defaultThreadFactory(),5,TimeUnit.SECONDS,512);

    @Bean
    public static HashedWheelTimer getTimer(){ return timer;}

    public static void main(String[] args) {
        TimerTask task = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.out.println("---run service-----");
                //任务执行完成后再把自己添加到任务solt上
                addTask(this);
            }
        };
        addTask(task);
    }






    public static void addTask(TimerTask task){
        //根据时长把task任务放到响应的solt上
        timer.newTimeout(task,2,TimeUnit.SECONDS);
    }
}
