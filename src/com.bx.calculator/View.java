package com.bx.calculator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class View extends JFrame {
    private final JTextField inputField;    // 输入文本框
    private final JLabel resultLabel;   // 结果标签

    public View() {
        setTitle("智能计算器");  // 设置标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(400, 500);
        setLocationRelativeTo(null);    // 窗口居中显示

        inputField = new JTextField();
        resultLabel = new JLabel();

        JPanel buttonPanel = new JPanel(new GridLayout(6, 4));  // 按钮面板，使用网格布局
        String[] buttonLabels = {
                "用户", "历史记录", "转二进制", "转十进制",
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "!", "+",
                "√", "^", "清除", "="
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setPreferredSize(new Dimension(80, 60)); // 设置按钮大小
            button.addActionListener(e -> {
                String command = e.getActionCommand();  // 获取按钮的命令
                String inputText = inputField.getText();    // 获取输入文本
                Controller.handleButtonAction(command, inputText, View.this);   // 调用控制器的处理按钮点击事件的方法
            });
            buttonPanel.add(button);
        }

        add(inputField, BorderLayout.NORTH);
        add(resultLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // 设置输入文本框的文本
    public void setInputText(String text) {
        inputField.setText(text);
    }

    // 设置结果标签的文本
    public void displayResult(String result) {
        resultLabel.setText(result);
    }

    // 显示用户选项
    public void displayUserOptions() {
        if (Controller.isLoggedIn()) {  // 如果已登录
            if (Controller.isRoot()) {
                displayRootOptionsMenu();
            } else {
                displayUserOptionsMenu();
            }
        } else {    // 如果未登录
            Object[] options = {"登录", "注册"};
            int choice = JOptionPane.showOptionDialog(this, "请选择操作：", "用户选项", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            switch (choice) {
                case 0 -> displayLoginDialog();
                case 1 -> displayRegistrationDialog();
            }
        }
    }

    // 显示登录对话框
    public void displayLoginDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
                "用户名:", usernameField,
                "密码:", passwordField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "用户登录", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            Controller.login(username, password);
        }
    }


    // 显示注册对话框
    public void displayRegistrationDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
                "用户名:", usernameField,
                "密码:", passwordField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "用户注册", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            Controller.register(username, password);
        }
    }

    // 显示已登录用户的选项菜单
    public void displayUserOptionsMenu() {
        Object[] options = {"修改密码", "注销", "退出"};
        int choice = JOptionPane.showOptionDialog(this, "请选择操作：", "用户选项", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        switch (choice) {
            case 0 -> displayChangePasswordDialog();
            case 1 -> displayCancelDialog();
            case 2 -> {
                Controller.logout();
                setTitle("智能计算器");
            }
        }
    }

    public void displayRootOptionsMenu() {
        Object[] options = {
                "全部用户", "修改密码", "注销", "退出"
        };
        int choice = JOptionPane.showOptionDialog(this, "请选择操作：", "管理员面板", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        switch (choice) {
            case 0 -> displayAllUser();
            case 1 -> displayChangePasswordDialog();
            case 2 -> displayCancelDialog();
            case 3 -> {
                Controller.logout();
                setTitle("智能计算器");
            }
        }
    }

    private void displayAllUser() {
        //未完成
        JOptionPane.showMessageDialog(this, "待完成！", "全部用户", JOptionPane.INFORMATION_MESSAGE);
    }

    // 显示修改密码对话框
    public void displayChangePasswordDialog() {
        JPasswordField oldPasswordField = new JPasswordField();
        JPasswordField newPasswordField = new JPasswordField();

        Object[] message = {
                "请输入旧密码:", oldPasswordField,
                "请输入新密码:", newPasswordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "修改密码", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());

            if (Controller.changePassword(oldPassword, newPassword)) {
                JOptionPane.showMessageDialog(this, "密码修改成功！", "密码修改", JOptionPane.INFORMATION_MESSAGE);    // 显示密码修改成功的信息框
            } else {
                JOptionPane.showMessageDialog(this, "旧密码不正确", "密码错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void displayCancelDialog() {
        int choice = JOptionPane.showConfirmDialog(null, "您确定要注销用户吗？", "确认注销", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            // 用户选择是，执行注销操作
            Controller.cancel();
            JOptionPane.showMessageDialog(null, "用户注销成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 用户选择否或关闭对话框，取消注销操作
            JOptionPane.showMessageDialog(null, "用户注销已取消。", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 显示计算历史记录
    public void displayHistory(List<String> history) {
        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);

        StringBuilder sb = new StringBuilder();
        for (String entry : history) {
            sb.append(entry).append("\n");
        }
        historyArea.setText(sb.toString());

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setPreferredSize(new Dimension(400, 400));

        JButton clearButton = new JButton("清除");
        clearButton.addActionListener(e -> {
            Controller.clearHistory();
            historyArea.setText("");
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(clearButton, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "历史记录", JOptionPane.PLAIN_MESSAGE);
    }

}
