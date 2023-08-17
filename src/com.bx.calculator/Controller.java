package com.bx.calculator;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

public class Controller {
    private static Model model;
    private static View view;

    public static void main(String[] args) {
        model = new Model();
        view = new View();
        view.setVisible(true);
    }

    // 处理按钮点击事件的方法，根据不同的命令执行相应的操作
    public static void handleButtonAction(String command, String inputText, View view) {
        switch (command) {
            case "用户" -> view.displayUserOptions();
            case "历史记录" -> {
                List<String> history = model.loadHistory();
                if (history != null) {
                    view.displayHistory(history);
                } else {
                    JOptionPane.showMessageDialog(view, "没有历史", "查询失败", JOptionPane.ERROR_MESSAGE); // 显示错误消息框，提示没有历史记录
                }
            }
            case "转二进制" -> {
                if (inputText.isEmpty()) {
                    view.displayResult("输入为空"); // 提醒输入为空
                } else {
                    view.displayResult(model.decimalToBinary(inputText));  // 将输入的文本转换为二进制
                }
            }
            case "转十进制" -> {
                if (inputText.isEmpty()) {
                    view.displayResult("输入为空"); // 提醒输入为空
                } else {
                    view.displayResult(model.binaryToDecimal(inputText));  // 将输入的文本转换为十进制
                }
            }
            case "清除" -> {
                view.setInputText("");  // 清空输入文本
                view.displayResult(""); // 清空结果
            }
            case "=" -> {
                if (inputText.isEmpty()) {
                    view.displayResult("输入为空"); // 提醒输入为空
                }
                view.displayResult(model.calculateExpression(inputText));// 计算表达式的结果
            }
            default -> view.setInputText(inputText + command);  // 将命令追加到输入文本中
        }
    }

    // 登录方法，根据用户名和密码进行用户登录
    public static void login(String username, String password) {
        User user = model.loginUser(username, password);
        if (user != null) {
            view.setTitle("计算器Pro - " + user.getUsername()); // 设置视图的标题，显示当前用户的用户名
        } else {
            JOptionPane.showMessageDialog(view, "登录失败！请检查用户名和密码。", "登录失败", JOptionPane.ERROR_MESSAGE); // 显示登录失败的错误消息框
        }

    }

    // 注册方法，根据用户名和密码进行用户注册
    public static void register(String username, String password) {
        try {
            model.addUser(username, password);
            JOptionPane.showMessageDialog(view, "注册成功！请使用新账号登录。", "注册成功", JOptionPane.INFORMATION_MESSAGE); // 显示注册成功的信息框
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "注册失败", JOptionPane.ERROR_MESSAGE); // 显示注册失败的错误消息框
        }
    }

    // 退出方法，清除当前用户信息
    public static void logout() {
        view.setTitle("计算器 Pro"); // 设置视图的标题为默认标题
        model.logout(); // 清除当前用户信息
    }

    public static void cancel() {
        model.deleteUser();
        logout();
    }

    // 判断用户是否已登录
    public static boolean isLoggedIn() {
        return model.getCurrentUser() != null;
    }

    //判断是否是管理员
    public static boolean isRoot() {
        return Objects.equals(model.getName(), "root");
    }

    // 修改密码方法
    public static boolean changePassword(String oldPassword, String newPassword) {
        return model.changePassword(oldPassword, newPassword);
    }

    public static void clearHistory() {
        model.clearHistory();

    }
}
