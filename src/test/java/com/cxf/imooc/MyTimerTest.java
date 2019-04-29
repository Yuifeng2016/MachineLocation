package com.cxf.imooc;

import com.cxf.imooc.netty.MyTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.junit.Test;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/27 21:27
 * @description：定时器测试类
 */
public class MyTimerTest {


    @Test
    public void testTimer(){

        TimerTask task = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.out.println("---run service-----");
                //任务执行完成后再把自己添加到任务solt上
                MyTimer.addTask(this);
            }
        };
        MyTimer.addTask(task);
    }
}
