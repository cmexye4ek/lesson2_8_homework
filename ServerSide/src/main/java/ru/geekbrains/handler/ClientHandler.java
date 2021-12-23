package ru.geekbrains.handler;

import ru.geekbrains.service.MyServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private MyServer myServer;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String nickName;

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
        while (true) {


            String message = dis.readUTF();
            if (message.startsWith("/login")) {
                String[] arr = message.split("-", 3);
                if (arr.length != 3) {
                    throw new IllegalAccessException();
                }
                final String nick = myServer.getAuthenticationService().getNickNameByLoginAndPassword(arr[1].trim(), arr[2].trim());
                if (nick != null) {
                    if (!myServer.nickIsBusy(nick)) {
                        sendMessage("/login" + nick);
                        this.nickName = nick;
                        myServer.sendMessageToClients(nickName + " connected to chat");
                        System.out.println("Client " + nickName + " logged in");
                        myServer.subscribe(this);
                        return;
                    } else {
                        sendMessage("Your " + nick + " is busy now");
                    }
                } else {
                    sendMessage("Wrong login or password");
                }
            }

        }
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage() throws Exception {
        while (true) {

            String message = dis.readUTF();
            if (message.startsWith("/")) {
                if (message.startsWith("/logout")) {
                    myServer.sendMessageToClients(nickName + " exit from chat");
                    System.out.println("Client " + nickName + " logged out");
//                    closeConnection();
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
                continue;
            }
                myServer.sendMessageToClients(nickName + ": " + message);

        }
    }


    private void closeConnection() {
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


}
