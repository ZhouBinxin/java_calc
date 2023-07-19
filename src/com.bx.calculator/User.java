package com.bx.calculator;

import java.io.Serializable;

public class User implements Serializable {
    private final String username;  // 用户名
    private String password;    //密码

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
