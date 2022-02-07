package ru.geekbrains.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.handler.ClientHandler;


import java.util.TimerTask;

public class AuthTimer extends TimerTask {
    private static final Logger LOGGER = LogManager.getLogger(AuthTimer.class.getName());
    private ClientHandler clientHandler;

    public AuthTimer(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void run() {
        if (!clientHandler.getAuthorized()) {
            clientHandler.sendMessage("/authTimeOut"); // бросает эксепшн когда клиент закрывает окно до выполнения задачи.
            LOGGER.info("Connection " + clientHandler.getConnectionToken().toString() + " authentication timeout, disconnected");
            clientHandler.closeConnection();
        }
    }
}
