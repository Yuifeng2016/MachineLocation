package com.cxf.imooc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/24 14:27
 * @description：测试用户类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {


    private static final long serialVersionUID = 2533177223601139311L;
    private Long id;
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
