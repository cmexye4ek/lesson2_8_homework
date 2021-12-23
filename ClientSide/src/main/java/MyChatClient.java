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

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean loginWindowFlag;


    public MyChatClient() {
        prepareGUI();
        try {
            openConnection();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Server connection error");
            msgInputField.setEditable(false);
            msgInputField.setText("You cant send message, connection is lost");

        }

    }

    public void openConnection() throws Exception {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        Thread thread = new Thread(() -> {
            try {
                chatArea.append("Connection to server established" + "\n");
                while (true) {

                    String message = dis.readUTF();

                    if (message.startsWith("/login")) {
                        nickName = message.substring(6);
                        chatArea.append("You have been successfully logging in as: " + nickName + "\n");
                        break;
                    }
                }

                while (true) {
                    String message = dis.readUTF();
                    if (message.startsWith("/logout")) {
                        chatArea.append("\n" + " You have been disconnected from server ");
                        msgInputField.setEditable(false);
                        msgInputField.setText("You cant send message, connection is closed");
                        closeConnection();
                        break;
                    }

                    chatArea.append(message + "\n");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();

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

    public void sendMessageToServer() {
        if (!loginInputField.getText().trim().isEmpty() && loginInputField != null && !passInputField.getText().trim().isEmpty() && passInputField != null) {
            String credentials = "/login-" + loginInputField.getText() + "-" + passInputField.getText();
            try {
                dos.writeUTF(credentials);
                loginWindowFlag = true; //эта проверка должна быть не здесь но разобраться как её вкорячить в поток проверки логина я не успел
            } catch (Exception e) {
                e.printStackTrace();
            }

            loginInputField.setText("");
            passInputField.setText("");

        }
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


        JPanel bottomPanel = new JPanel(new BorderLayout());
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


        JPanel loginPanel = new JPanel(new BorderLayout());
        JPanel loginPanel2 = new JPanel(new GridLayout());
        JLabel loginTip = new JLabel("Login:");
        JLabel passwordTip = new JLabel("Password:");
        loginInputField = new JTextField();
        passInputField = new JTextField();
        JButton btnLogin = new JButton("Login");
        add(loginPanel, BorderLayout.CENTER);
        GridLayout layout = new GridLayout(3, 2, 1, 1);
        loginPanel2.setLayout(layout);
        loginPanel.add(loginPanel2);
        loginPanel2.add(loginTip);
        loginPanel2.add(loginInputField);
        loginPanel2.add(passwordTip);
        loginPanel2.add(passInputField);
        loginPanel2.add(btnLogin);
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    sendMessageToServer();

                } catch (Exception exc) {
                    exc.printStackTrace();
                }
                if (loginWindowFlag) {
                    loginPanel.setVisible(false);
                    loginPanel2.setVisible(false);
                    bottomPanel.setVisible(true);
                    add(new JScrollPane(chatArea), BorderLayout.CENTER);
                }

            }

        });


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                if (dos != null) {
                    try {
                        dos.writeUTF("/logout");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        setVisible(true);


    }
}

