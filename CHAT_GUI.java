package chat;

import javax.swing.*;
import java.awt.*;

public class CHAT_GUI extends JFrame {
    Container CP;
    JPanel CHAT_SUBBOX_P;
    JLabel WARNING_LB;
    JTextField CHAT_TF;
    JButton SEND_BT;
    JTextArea CHATBOX_TA;
    JScrollPane CHAT_MAINBOX_SP;

    CHAT_GUI() {
        CP = getContentPane();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("CHAT");
        setSize(400, 700);
        CP.setLayout(new BorderLayout());

        CHATBOX_TA = new JTextArea();

        CHAT_MAINBOX_SP = new JScrollPane(CHATBOX_TA);
        CHAT_MAINBOX_SP.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        CHAT_SUBBOX_P = new JPanel(new BorderLayout());
        CHAT_SUBBOX_P.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        CHAT_TF = new JTextField();
        SEND_BT = new JButton("Send");

        CHAT_SUBBOX_P.add(CHAT_TF, BorderLayout.CENTER);
        CHAT_SUBBOX_P.add(SEND_BT, BorderLayout.EAST);

        CP.add(CHAT_MAINBOX_SP, BorderLayout.CENTER);
        CP.add(CHAT_SUBBOX_P, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String[] args) {
        new CHAT_GUI();
    }
}
