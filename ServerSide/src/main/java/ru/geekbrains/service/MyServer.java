package ru.geekbrains.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.handler.ClientHandler;
import ru.geekbrains.service.interfaces.AuthenticationService;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MyServer {
    private static final Logger LOGGER = LogManager.getLogger(MyServer.class.getName());
    private static final Logger LOGGER_MSG = LogManager.getLogger("messages");
    private static final Integer PORT = 8880;

    private AuthenticationService authenticationService;
    private ExecutorService executorService;
    private List<ClientHandler> handlerList;

    public MyServer() {
        LOGGER.info("Server started");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            authenticationService = new AuthenticationServiceImpl();
            authenticationService.start();
            handlerList = new ArrayList<>();
            executorService = Executors.newFixedThreadPool(10);
            while (true) {
                LOGGER.info("Server waiting connection");
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket, executorService);
            }

        } catch (Exception e) {
            LOGGER.error("Socket error", e);
        } finally {
            authenticationService.stop();
            executorService.shutdown();
        }
    }

    public synchronized boolean nickIsBusy(String nickName) {
        return handlerList
                .stream()
                .anyMatch(clientHandler -> clientHandler.getNickName().equalsIgnoreCase(nickName));
    }

    public synchronized void sendWhisperMessage(ClientHandler from, String to, String message) {
        handlerList.forEach(clientHandler -> {
            if (clientHandler.getNickName().equals(to)) {
                clientHandler.sendMessage(from.getNickName() + " whisper you: " + message);
                from.sendMessage("You whisper to " + to + ": " + message);
                LOGGER_MSG.log(Level.INFO, from.getNickName() + " whisper to " + to + ": " + message);
                return;
            }

        });
        if (!nickIsBusy(to)) {
            from.sendMessage("User with nick " + to + " is offline");
        }
    }

    public synchronized void sendMessageToClients(String message) {
        handlerList.forEach(clientHandler -> clientHandler.sendMessage(message));
        LOGGER_MSG.log(Level.INFO, message);

    }

    public synchronized void getOnlineUsers(ClientHandler clientHandler) {
        String str = "Now online:\n";
        for (ClientHandler ch : handlerList) {
            if (ch.getNickName().equals(clientHandler.getNickName())) {
                continue;
            }
            str = str.concat(ch.getNickName() + "\n");
        }
        clientHandler.sendMessage(str);
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        handlerList.add(clientHandler);
    }

    public synchronized void unSubscribe(ClientHandler clientHandler) {
        handlerList.remove(clientHandler);
    }

    public synchronized AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }
}
