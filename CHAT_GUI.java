package chat;

import javax.swing.*;
import java.awt.*;

public class CHAT_GUI extends JFrame {
    Container CP;
    JPanel CHAT_HEADBOX_P, CHAT_FOOTBOX_P;
    JTextField CHAT_TF, RECEIVER_TF;
    JButton SEND_BT;
    JTextArea CHATBOX_TA;
    JScrollPane CHAT_MAINBOX_SP;

    CHAT_GUI() {
        CP = getContentPane();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("CHAT");
        setSize(400, 700);
        CP.setLayout(new BorderLayout());


        RECEIVER_TF = new JTextField();

        CHAT_HEADBOX_P = new JPanel(new BorderLayout());
        CHAT_HEADBOX_P.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        CHAT_HEADBOX_P.add(RECEIVER_TF, BorderLayout.CENTER);

        CHATBOX_TA = new JTextArea();

        CHAT_MAINBOX_SP = new JScrollPane(CHATBOX_TA);
        CHAT_MAINBOX_SP.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        CHAT_FOOTBOX_P = new JPanel(new BorderLayout());
        CHAT_FOOTBOX_P.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        CHAT_TF = new JTextField();
        SEND_BT = new JButton("Send");

        CHAT_FOOTBOX_P.add(CHAT_TF, BorderLayout.CENTER);
        CHAT_FOOTBOX_P.add(SEND_BT, BorderLayout.EAST);

        CP.add(CHAT_HEADBOX_P, BorderLayout.NORTH);
        CP.add(CHAT_MAINBOX_SP, BorderLayout.CENTER);
        CP.add(CHAT_FOOTBOX_P, BorderLayout.SOUTH);

        setVisible(false);
    }

//    public static void main(String[] args) {
//        new CHAT_GUI();
//    }
}
