package ru.geekbrains.handler;

import ru.geekbrains.service.MyServer;
import ru.geekbrains.service.AuthTimer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;

public class ClientHandler {

    private MyServer myServer;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private AuthTimer authTimer;


    private String nickName;
    Random random = new Random();
    private Integer connectionToken = random.nextInt(1000000); // for testing purposes
    private boolean authFlag = false;


    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    authentication();
                    receiveMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void authentication() throws Exception {
        System.out.println("Client " + connectionToken.toString() + " connected to server");
        authTimer = new AuthTimer(this);
        Timer timer = new Timer();
        timer.schedule(authTimer, 10000);
        while (true) {

            String message = dis.readUTF();

            if (message.startsWith("/login")) {
                String login = message.split("-", 3)[1];
                String password = message.split("-", 3)[2];
                String nick = myServer.getAuthenticationService().authentication(login.trim(), password.trim());
                if (nick != null) {
                    if (!myServer.nickIsBusy(nick)) {
                        this.nickName = nick;
                        sendMessage("/successLogin" + nickName);
                        myServer.sendMessageToClients(nickName + " connected to chat");
                        System.out.println("Client " + connectionToken.toString() + " logged in as " + nickName);
                        myServer.subscribe(this);
                        authFlag = true;
                        return;
                    } else {
                        sendMessage("/error1" + nick); //nick is busy
                    }
                } else {
                    sendMessage("/error2"); //wrong login or password
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
                        System.out.println("Client " + nickName + " logged out");
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
            e.printStackTrace();
        }
    }


    public void closeConnection() {
        myServer.unSubscribe(this);
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickName() {
        return nickName;
    }

    public Integer getConnectionToken() {
        return connectionToken;
    }

    public boolean getAuthorized() {
        return authFlag;
    }

}
