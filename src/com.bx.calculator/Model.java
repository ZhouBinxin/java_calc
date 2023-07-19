package com.bx.calculator;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;


public class Model {
    private User currentUser;   // 当前用户
    private final Map<String, User> users;  //用户列表
    private Connection connection; // 数据库连接对象

    public Model() {
        users = new HashMap<>();    // 初始化用户列表
        establishDatabaseConnection(); // 建立数据库连接
        loadUsers();    // 加载用户数据
    }

    // 计算表达式
    // 计算表达式
    public String calculateExpression(String inputText) {
        try {
            String expression = preprocessInput(inputText);
            double result = calculate(expression);
            saveCalculation(inputText + " = " + result);
            return String.valueOf(result);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String preprocessInput(String inputText) {
        // 移除空格和多余的字符
        String expression = inputText.replaceAll("\\s+", "").replaceAll(",", "");

        // 在运算符两侧添加空格
        expression = expression.replaceAll("([+\\-*/()√])", " $1 ");

        return expression;
    }

    public static double calculate(String expression) {
        Stack<Double> numberStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (Character.isDigit(ch)) {
                StringBuilder numBuilder = new StringBuilder();
                numBuilder.append(ch);

                // 继续读取数字的其他字符
                while (i + 1 < expression.length() && (Character.isDigit(expression.charAt(i + 1)) || expression.charAt(i + 1) == '.')) {
                    numBuilder.append(expression.charAt(i + 1));
                    i++;
                }

                double num = Double.parseDouble(numBuilder.toString());
                numberStack.push(num);
            } else if (ch == '(') {
                operatorStack.push(ch);
            } else if (ch == ')') {
                // 处理嵌套的开方运算
                while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                    performOperation(numberStack, operatorStack);
                }
                operatorStack.pop(); // 弹出 '('
            } else if (isOperator(ch)) {
                while (!operatorStack.isEmpty() && precedence(operatorStack.peek()) >= precedence(ch)) {
                    performOperation(numberStack, operatorStack);
                }
                operatorStack.push(ch);
            } else if (ch == '!') { // 处理阶乘运算符
                double num = numberStack.pop();
                double factorialResult = performFactorial(String.valueOf((int) num));
                numberStack.push(factorialResult);
            } else if (ch == '^') { // 处理幂运算符
                operatorStack.push(ch);
            } else if (ch == '√') { // 处理开方运算符
                operatorStack.push(ch);
            }
        }

        while (!operatorStack.isEmpty()) {
            performOperation(numberStack, operatorStack);
        }

        return numberStack.pop();
    }

    private static boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    private static int precedence(char ch) {
        if (ch == '+' || ch == '-') {
            return 1;
        } else if (ch == '*' || ch == '/') {
            return 2;
        } else if (ch == '^' || ch == '√') {
            return 3;
        }
        return 0;
    }

    private static void performOperation(Stack<Double> numberStack, Stack<Character> operatorStack) {
        char operator = operatorStack.pop();
        if (operator == '√') {
            double num = numberStack.pop();
            double sqrtResult = Math.sqrt(num);
            numberStack.push(sqrtResult);
        } else {
            double num2 = numberStack.pop();
            double num1 = numberStack.pop();
            double result = switch (operator) {
                case '+' -> num1 + num2;
                case '-' -> num1 - num2;
                case '*' -> num1 * num2;
                case '/' -> num1 / num2;
                case '^' -> Math.pow(num1, num2);
                default -> 0;
            };
            numberStack.push(result);
        }
    }

    // 执行阶乘运算
    public static long performFactorial(String inputText) {
        long result = 1;
        for (long i = 1; i <= Long.parseLong(inputText); i++) {
            result = result * i;
        }
        return result;
    }


    // 登录用户
    public User loginUser(String username, String password) {
        if (users.containsKey(username)) {
            User user = users.get(username);
            if (user.getPassword().equals(password)) {
                currentUser = user;
                return user;
            }
        }
        return null;
    }

    // 注册用户
    public void addUser(String username, String password) {
        if (Objects.equals(username, "root")) {
            throw new IllegalArgumentException("用户名非法！");
        } else if (users.containsKey(username)) {
            throw new IllegalArgumentException("用户已存在，请重新注册！");
        } else if (Objects.equals(username, "") || Objects.equals(password, "")) {
            throw new IllegalArgumentException("注册失败！请检查用户名和密码。");
        } else {
            User user = new User(username, password);
            users.put(username, user);
            saveUsers();
        }
    }

    public void deleteUser() {
        if (currentUser != null) {
            File historyFile = new File(getHistoryFilePath());
            if (historyFile.exists()) {
                if (historyFile.delete()) {
                    System.out.println("历史记录文件已成功删除");
                } else {
                    System.out.println("无法删除历史记录文件");
                }
            } else {
                System.out.println("历史记录文件不存在");
            }

            users.remove(currentUser.getUsername());
            saveUsers();
            loadUsers();
        }
    }

    // 获取当前账号
    public User getCurrentUser() {
        return currentUser;
    }

    //判断是否是管理员
    public String getName() {
        return currentUser.getUsername();
    }

    // 建立数据库连接
    private void establishDatabaseConnection() {
        String url = "jdbc:mysql://localhost:3306/calc"; // 替换为你的数据库URL
        String username = "root"; // 替换为你的数据库用户名
        String password = "123456"; // 替换为你的数据库密码

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 保存用用户数据
    private void saveUsers() {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            for (User user : users.values()) {
                statement.setString(1, user.getUsername());
                statement.setString(2, user.getPassword());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 从文件加载用户数据
    private void loadUsers() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users")) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                users.put(username, new User(username, password));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getHistoryFilePath() {
        String path = "history.dat";
        if (currentUser != null) {
            path = currentUser.getUsername() + path;
        }
        return path;
    }

    // 保存计算历史到文件
    private void saveCalculation(String calculation) {
        String path = getHistoryFilePath();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            // 获取当前日期和时间
            LocalDateTime now = LocalDateTime.now();

            // 提取月份、日和小时
            int month = now.getMonthValue();
            int day = now.getDayOfMonth();
            int hour = now.getHour();

            bw.write(month + "月" + day + "日" + hour + "时  " + calculation);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从文件加载计算历史
    public List<String> loadHistory() {
        String path = getHistoryFilePath();
        List<String> history = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return history;
    }

    //清除历史记录
    public void clearHistory() {
        String path = getHistoryFilePath();
        try {
            File file = new File(path);
            FileWriter writer = new FileWriter(file);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将十进制数字转换为二进制
    public String decimalToBinary(String inputText) {
        try {
            int decimal = Integer.parseInt(inputText);
            String result = inputText + " 的二进制为: " + Integer.toBinaryString(decimal);
            saveCalculation(result);
            return result;
        } catch (NumberFormatException ex) {
            return "错误的输入";
        }
    }

    // 将二进制数字转换为十进制
    public String binaryToDecimal(String inputText) {
        try {
            int decimal = Integer.parseInt(inputText, 2);
            String result = inputText + " 的十进制为: " + decimal;
            saveCalculation(result);
            return result;
        } catch (NumberFormatException ex) {
            return "错误的输入";
        }
    }

    public void logout() {
        currentUser = null;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser.getPassword().equals(oldPassword)) {
            currentUser.setPassword(newPassword);
            loadUsers();
            return true;
        } else {
            return false;
        }
    }
}
