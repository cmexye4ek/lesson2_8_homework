package ru.geekbrains.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.handler.ClientHandler;
import ru.geekbrains.service.interfaces.AuthenticationService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger LOGGER = LogManager.getLogger(AuthenticationServiceImpl.class.getName());
    private Connection dbConnector;
    private Statement statement;


    public AuthenticationServiceImpl() {

    }

    @Override
    public void start() throws SQLException {
        dbConnector = DBConnector.getConnection();
        statement = dbConnector.createStatement();
        LOGGER.info("Authentication service started");
    }

    @Override
    public void stop() {
        closeConnection();
        LOGGER.info("Authentication service stopped");
    }

    @Override
    public String authentication(String login, String password) throws SQLException {
        ResultSet credentialsSet = statement.executeQuery("SELECT * FROM users WHERE Login = '" + login + "' AND Password = '" + password + "'");
        if (credentialsSet.next()) {
            return credentialsSet.getString("Nickname");
        } else {
            return null;
        }
    }

    @Override
    public void changeNickName(ClientHandler from, String newNickName) throws SQLException {
        statement.executeUpdate("UPDATE users SET Nickname = '" + newNickName + "' WHERE Nickname = '" + from.getNickName() + "' AND Login = '"+ from.getLogin() +"';");
    }

    private void closeConnection() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.error("Authentication server error (Statement closing)", e);
            }
        }

        if (dbConnector != null) {
            try {
                dbConnector.close();
            } catch (SQLException e) {
                LOGGER.error("Authentication server error (DBConnector closing)", e);
            }
        }
    }
}
