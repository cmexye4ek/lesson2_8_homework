import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MyChatClient extends JFrame {
    private final String SERVER_ADDRESS = "127.0.0.1";
    private final int SERVER_PORT = 8880;
    private String nickName;


    private JTextField msgInputField;
    private JTextArea chatArea;
    private JTextField loginInputField;
    private JTextField passInputField;
    private JLabel serviceMsgTip;
    private JPanel loginPanel;
    private JPanel bottomPanel;
    private JButton btnLogin;


    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean authenticated = false;


    public MyChatClient() {
        prepareGUI();
        connection();
    }

    public void connection() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            serviceMsgTip.setText("Connection to server established, please login");
            new Thread(() -> {
                try {
                    while (true) {


                        String message = dis.readUTF();
                        if (message.startsWith("/")) {
                            if (message.startsWith("/successLogin")) {
                                nickName = message.substring(13);
                                chatArea.append("You have been successfully logging in as: " + nickName + "\n");
                                authenticated = true;
                                loginWindowSwitch(authenticated);

                            }

                            if (message.startsWith("/authTimeOut")) {
                                serviceMsgTip.setText("Authentication timeout");
                                return;

                            }

                            if (message.startsWith("/error1")) {
                                nickName = message.substring(7);
                                serviceMsgTip.setText("Your nickname " + nickName + " is busy now");

                            }

                            if (message.startsWith("/error2")) {
                                serviceMsgTip.setText("Wrong login or password");

                            }

                            if (message.startsWith("/logout")) {
//                                chatArea.append("\n" + " You have been disconnected from server ");
//                                msgInputField.setEditable(false);
//                                msgInputField.setText("You cant send message, connection is closed");
                                authenticated = false;
                                loginWindowSwitch(authenticated);
                                return;
                            }
                        } else {
                            chatArea.append(message + "\n");
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            serviceMsgTip.setText("Connection to server lost");
        }
    }

    public void closeConnection() {
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

    public void loginButtonClick() {

        if (socket == null || socket.isClosed()) {
            connection();
        }
        if (!loginInputField.getText().trim().isEmpty() && loginInputField != null && !passInputField.getText().trim().isEmpty() && passInputField != null) {
            String credentials = "/login-" + loginInputField.getText() + "-" + passInputField.getText();
            try {
                dos.writeUTF(credentials);
            } catch (Exception e) {
                e.printStackTrace();
            }

            loginInputField.setText("");
            passInputField.setText("");
            loginInputField.grabFocus();

        }

    }

    public void sendMessageToServer() {

        if (!msgInputField.getText().trim().isEmpty() && msgInputField != null) {
            try {
                dos.writeUTF(msgInputField.getText());
                msgInputField.setText("");
                msgInputField.grabFocus();

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Message send error, server connection is lost");
                msgInputField.setEditable(false);
                msgInputField.setText("You cant send message, connection is lost");

            }
        }

    }

    public void prepareGUI() {

        setBounds(600, 300, 500, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);


        bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Send");
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        msgInputField = new JTextField();
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);
        btnSendMsg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageToServer();
            }
        });
        msgInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageToServer();

            }
        });

        bottomPanel.setVisible(false);

        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        JLabel loginTip = new JLabel("Login:");
        JLabel passwordTip = new JLabel("Password:");
        serviceMsgTip = new JLabel();
        loginInputField = new JTextField();
        passInputField = new JTextField();
        btnLogin = new JButton("Login");
        add(loginPanel, BorderLayout.CENTER);
        constraints.gridx = 1;
        constraints.gridy = 1;
        loginPanel.add(loginTip, constraints);
        constraints.ipadx = 100;
        constraints.gridy = 2;
        loginPanel.add(loginInputField, constraints);
        constraints.gridy = 3;
        constraints.ipadx = 0;
        loginPanel.add(passwordTip, constraints);
        constraints.gridy = 4;
        constraints.ipadx = 100;
        loginPanel.add(passInputField, constraints);
        constraints.gridy = 5;
        constraints.ipadx = 0;
        loginPanel.add(btnLogin, constraints);
        constraints.gridy = 6;
        loginPanel.add(serviceMsgTip, constraints);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    loginButtonClick();

                } catch (Exception exc) {
                    exc.printStackTrace();
                }

            }
        });


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                if (socket != null) {
                    if (!socket.isClosed()) {
                        try {
                            dos.writeUTF("/logout");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        setVisible(true);

    }

    public void loginWindowSwitch(Boolean authenticated) {                  //метод для переключения окна логина
        if (authenticated) {
            loginPanel.setVisible(false);
            add(new JScrollPane(chatArea), BorderLayout.CENTER);
            chatArea.setVisible(true);
            bottomPanel.setVisible(true);
        } else {
            /*
            Здесь есть проблема с отрисовкой chatArea после повторного логина, отправка любого сообщения отрисовывает панель нормально.
            Переделать создание окна в несколько методов, чтобы работало.
             */
            chatArea.setText(null);
            chatArea.setVisible(false);
            bottomPanel.setVisible(false);
            loginPanel.setVisible(true);
        }

    }

}

