package ru.geekbrains.service;

import ru.geekbrains.handler.ClientHandler;


import java.util.TimerTask;

public class AuthTimer extends TimerTask {

    private ClientHandler clientHandler;

    public AuthTimer(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void run() {
        if (!clientHandler.getAuthorized()) {
            clientHandler.sendMessage("/authTimeOut"); // бросает эксепшн когда клиент закрывает окно до выполнения задачи.
            System.out.println(clientHandler.getConnectionToken().toString() + " authentication timeout, disconnected");
            clientHandler.closeConnection();
        }
    }
}

