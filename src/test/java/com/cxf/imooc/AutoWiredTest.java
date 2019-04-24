package com.cxf.imooc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/24 23:00
 * @description：Autowired注解测试类
 * @version:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AutoWiredTest {
    @Autowired
    private RedisTemplate<String, Serializable> redisCacheTemplate;
    @Test
    public void test(){
        System.out.println("test");

    }
}
