package ru.geekbrains.service;

import ru.geekbrains.handler.ClientHandler;
import ru.geekbrains.service.interfaces.AuthenticationService;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MyServer {

    private static final Integer PORT = 8880;

    private AuthenticationService authenticationService;
    private List<ClientHandler> handlerList;

    public MyServer() {
        System.out.println("Server started");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            authenticationService = new AuthenticationServiceImpl();
            authenticationService.start();
            handlerList = new ArrayList<>();
            while (true) {
                System.out.println("Server waiting connection");
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            authenticationService.stop();
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
                return;
            }

        });
        if (!nickIsBusy(to)) {
            from.sendMessage("User with nick " + to + " is offline");
        }
    }

    public synchronized void sendMessageToClients(String message) {
        handlerList.forEach(clientHandler -> clientHandler.sendMessage(message));
    }

    public synchronized void getOnlineUsers(ClientHandler clientHandler) {
        String str = new String("Now online:\n");
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
