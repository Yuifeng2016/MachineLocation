package com.cxf.imooc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.cxf.imooc.dao")
@SpringBootApplication
public class ImoocApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImoocApplication.class, args);
    }

}
