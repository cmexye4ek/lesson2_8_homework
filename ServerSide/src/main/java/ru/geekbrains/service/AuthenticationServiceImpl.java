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
        ResultSet credentialsSet = statement.executeQuery("SELECT * FROM users");
        while (credentialsSet.next()) {
            if (credentialsSet.getString("Login").equals(login) && credentialsSet.getString("Password").equals(password)) {
                return credentialsSet.getString("Nickname");
            }
        }
        return null;
    }

//    private void createTable() throws SQLException {
//        statement.executeUpdate("CREATE TABLE IF NOT EXISTS Users (\n"
//                + "id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
//                + "Login TEXT, \n"
//                + "Password TEXT, \n"
//                + "Nickname TEXT\n"
//                + ")");
//    }
//
//    private void insertData() throws SQLException {
//        statement.executeUpdate("INSERT INTO users (Login, Password, Nickname)\n"
//                + "VALUES ('A', 'A', 'A');");
//        statement.executeUpdate("INSERT INTO users (Login, Password, Nickname)\n"
//                + "VALUES ('B', 'B', 'B');");
//        statement.executeUpdate("INSERT INTO users (Login, Password, Nickname)\n"
//                + "VALUES ('C', 'C', 'C');");
//    }

    public void changeNickName(ClientHandler from, String newNickName) throws SQLException {
        preparedStatement = dbConnector.prepareStatement("UPDATE users SET Nickname = ? WHERE Nickname = ?;");
        preparedStatement.setString(1, newNickName);
        preparedStatement.setString(2, from.getNickName());
        preparedStatement.addBatch();
        preparedStatement.executeBatch();
    }

    public List<String> getNickNameList() throws SQLException {
        List<String> nickList = new ArrayList<>();
        ResultSet credentialsSet = statement.executeQuery("SELECT * FROM users");
        while (credentialsSet.next()) {
            nickList.add(credentialsSet.getString("Nickname"));
        }
        return nickList;
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
