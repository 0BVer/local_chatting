package chat;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SIGNIN_GUI extends JFrame {
    Container CP;
    JPanel SIGN_IN_P;
    JLabel ID_LB, PW_LB, WARNING_LB;
    JTextField ID_TF, PW_TF;
    JButton CONFIRM_BT, SIGN_MODE_BT;

    SIGNIN_GUI(){
        CP = getContentPane();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("CHAT");
        setSize(400, 500);
        CP.setLayout(null);

        SIGN_IN_P = new JPanel(new GridLayout(3,1, 10, 10));
        SIGN_IN_P.setBorder(new TitledBorder(new LineBorder(Color.BLACK)));
        SIGN_IN_P.setBounds(100, 50, 200, 100);

        ID_LB = new JLabel("ID", JLabel.CENTER);
        ID_TF = new JTextField(10);

        PW_LB = new JLabel("PW", JLabel.CENTER);
        PW_TF = new JPasswordField(10);

        CONFIRM_BT = new JButton("Sign in");
        SIGN_MODE_BT = new JButton("Sign up");

        WARNING_LB = new JLabel("");

        SIGN_IN_P.add(ID_LB);
        SIGN_IN_P.add(ID_TF);
        SIGN_IN_P.add(PW_LB);
        SIGN_IN_P.add(PW_TF);
        SIGN_IN_P.add(CONFIRM_BT);
        SIGN_IN_P.add(SIGN_MODE_BT);
        SIGN_IN_P.add(WARNING_LB);

        CP.add(SIGN_IN_P);
        setVisible(true);
    }
}
