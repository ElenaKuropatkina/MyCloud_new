package com.elenakuropatkina.my.cloud.server;

import java.sql.*;

public class AuthService {


    private static Connection connection;
    private static Statement stmt;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("Драйвер подключен");
            connection = DriverManager.getConnection("jdbc:sqlite:myDB.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkLoginAndPass(String login, String pass) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT passHash FROM users WHERE login = '" + login + "'");
            int userHash = pass.hashCode();
            if (rs.next()) {
                int dbHash = rs.getInt(1);
                if (dbHash == userHash) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
