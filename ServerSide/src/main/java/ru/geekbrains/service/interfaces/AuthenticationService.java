package ru.geekbrains.service.interfaces;

public interface AuthenticationService {
    void start();
    void stop();
    String getNickNameByLoginAndPassword (String login, String password);
}
