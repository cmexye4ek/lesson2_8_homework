package ru.geekbrains.service;

import ru.geekbrains.handler.ClientHandler;
import ru.geekbrains.service.interfaces.AuthenticationService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationServiceImpl implements AuthenticationService {

    //        private List<UserEntity> userEntityList;
    private Connection dbConnector;
    private Statement statement;
    private PreparedStatement preparedStatement;

    public AuthenticationServiceImpl() {

    }

    @Override
    public void start() throws SQLException {
        dbConnector = DBConnector.getConnection();
        statement = dbConnector.createStatement();
//        createTable();  // создание бд
//        insertData(); // первоначальное заполнение бд
        System.out.println("Authentication service started");
    }

    @Override
    public void stop() {
        closeConnection();
        System.out.println("Authentication service stopped");
    }

    @Override
    public String authentication(String login, String password) throws SQLException {
        ResultSet credentialsSet = statement.executeQuery("SELECT * FROM users WHERE Login LIKE '" + login + "' AND Password LIKE '" + password + "'");
        if (credentialsSet.next()) {
            return credentialsSet.getString("Nickname");
        } else {
            return null;
        }
    }

    @Override
    public void changeNickName(ClientHandler from, String newNickName) throws SQLException {
        statement.executeUpdate("UPDATE users SET Nickname = '" + newNickName + "' WHERE Nickname LIKE '" + from.getNickName() + "' AND Login LIKE '"+ from.getLogin() +"';");
    }

    private void closeConnection() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (preparedStatement != null) {
            try {
                dbConnector.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (dbConnector != null) {
            try {
                dbConnector.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
