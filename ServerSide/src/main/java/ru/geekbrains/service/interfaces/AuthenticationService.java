package ru.geekbrains.service.interfaces;

import ru.geekbrains.handler.ClientHandler;

import java.sql.SQLException;

public interface AuthenticationService {
    void start() throws SQLException;
    void stop();
    String authentication (String login, String password) throws SQLException;
    void changeNickName(ClientHandler from, String newNickName) throws SQLException;
}
