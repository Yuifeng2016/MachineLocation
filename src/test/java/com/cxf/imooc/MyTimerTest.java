package com.cxf.imooc;

import com.cxf.imooc.netty.MyTimer;
import com.cxf.imooc.service.HttpService;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/27 21:27
 * @description：定时器测试类
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MyTimerTest {

    @Autowired
    HttpService service;

    @Test
    public void testTimer(){
        String taskId = "5f78c170ece74b27bf0faea6971c22ad";
        String username = "southXF";
        String token = "0ef9c9927a89e697632fa5fbc85458b0b9f73a49bece4ce5a401a9ef9374bf1a4ea92dd67678cd93";
        Map<String,String> param = new HashMap<>();
        param.put("taskId",taskId);
        param.put("username",username);
        param.put("token",token);
        String programId = service.getProgramId(param);
        System.out.println(programId);
    }
}
