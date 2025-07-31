package com.example.f_f;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQLConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/fuckyou?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
        String user = "root";
        String password = "gusdndldlek12!";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✅ MySQL 연결 성공!");
        } catch (Exception e) {
            System.out.println("❌ MySQL 연결 실패:");
            e.printStackTrace();
        }
    }
}
