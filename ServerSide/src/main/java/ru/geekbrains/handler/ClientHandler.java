package ru.geekbrains.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.service.MyServer;
import ru.geekbrains.service.AuthTimer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ExecutorService;

public class ClientHandler {

    private MyServer myServer;
    private Socket socket;
    private ExecutorService executorService;
    private DataInputStream dis;
    private DataOutputStream dos;
    private AuthTimer authTimer;
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class.getName());
    private String login;
    private String password;
    private String nickName;
    Random random = new Random();
    private Integer connectionToken = random.nextInt(1000000); // for testing purposes
    private boolean authFlag = false;


    public ClientHandler(MyServer myServer, Socket socket, ExecutorService executorService) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.executorService = executorService;
            executorService.execute(() -> {
                try {
                    authentication();
                    receiveMessage();
                } catch (Exception e) {
                    LOGGER.error("ClientHandler threads creation error", e);
                } finally {
                    closeConnection();
                }
            });
        } catch (Exception e) {
            LOGGER.error("ClientHandler creation  error(Server or IO streams)", e);
        }
    }


    public void authentication() throws Exception {
        LOGGER.info("Client " + connectionToken.toString() + " connected to server");
        authTimer = new AuthTimer(this);
        Timer timer = new Timer();
        timer.schedule(authTimer, 10000);
        while (true) {

            String message = dis.readUTF();

            if (message.startsWith("/login")) {
                login = message.split("-", 3)[1];
                password = message.split("-", 3)[2];
                String nick = myServer.getAuthenticationService().authentication(login.trim(), password.trim());
                if (nick != null) {
                    if (!myServer.nickIsBusy(nick)) {
                        this.nickName = nick;
                        sendMessage("/successLogin" + nickName);
                        myServer.sendMessageToClients(nickName + " connected to chat");
                        LOGGER.info("Client " + connectionToken.toString() + " logged in as " + nickName);
                        myServer.subscribe(this);
                        authFlag = true;
                        return;
                    } else {
                        sendMessage("/error1" + nick); //nick is busy
                        LOGGER.info("Client " + this.connectionToken + ", nick is busy");
                    }
                } else {
                    sendMessage("/error2"); //wrong login or password
                    LOGGER.info("Client " + this.connectionToken + ", wrong login or password");
                }
            }
        }
    }

    public void receiveMessage() throws Exception {
        while (true) {

            String message = dis.readUTF();
            if (message.startsWith("/")) {
                if (message.startsWith("/logout")) {
                    myServer.sendMessageToClients(nickName + " exit from chat");
                    if (nickName != null) {
                        LOGGER.info("Client " + nickName + " logged out");
                    }
                    sendMessage("/logout");
                    return;
                }

                if (message.startsWith("/w")) {
                    String to = message.split("-", 3)[1];
                    String msg = message.split("-", 3)[2];
                    myServer.sendWhisperMessage(this, to, msg);
                }

                if (message.startsWith("/online")) {
                    myServer.getOnlineUsers(this);
                    LOGGER.info("Client " + this.nickName + " send command: " + message);
                }

                if (message.startsWith("/changeNickname")) {
                    String newNickName = message.split("-", 2)[1];
                    if (!myServer.nickIsBusy(newNickName)) {
                        myServer.getAuthenticationService().changeNickName(this, newNickName);
                        myServer.sendMessageToClients(this.nickName + " has changed name to " + newNickName);
                        this.nickName = newNickName;
                    } else {
                        sendMessage("Desired nickname is busy now");
                    }
                }
                continue;
            }

            myServer.sendMessageToClients(nickName + ": " + message);

        }

    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            LOGGER.error("ClientHandler send message error", e);
        }
    }


    public void closeConnection() {
        myServer.unSubscribe(this);
        try {
            dis.close();
        } catch (IOException e) {
            LOGGER.error("ClientHandler error (dis closing)", e);
        }
        try {
            dos.close();
        } catch (IOException e) {
            LOGGER.error("ClientHandler error (dos closing)", e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.error("ClientHandler error (socket closing)", e);
        }
    }

    public String getNickName() {
        return nickName;
    }

    public String getLogin() {
        return login;
    }

    public Integer getConnectionToken() {
        return connectionToken;
    }

    public boolean getAuthorized() {
        return authFlag;
    }

}
