package chat;

import javax.swing.*;
import java.awt.*;

public class SIGNUP_GUI extends JFrame {
    Container CP;
    JPanel SIGNUP_P;
    JLabel ID_LB, PW_LB, PWCF_LB, WARNING_LB;
    JTextField ID_TF, PW_TF, PWCF_TF;
    JButton CONFIRM_BT, SIGN_MODE_BT;

    SIGNUP_GUI() {
        CP = getContentPane();
        setTitle("Sign UP");
        setSize(400, 500);
        CP.setLayout(new FlowLayout());

        SIGNUP_P = new JPanel(new GridLayout(4, 2, 5, 5));
        ID_LB = new JLabel("ID");
        ID_TF = new JTextField(10);
        PW_LB = new JLabel("PW");
        PW_TF = new JPasswordField(10);
        PWCF_LB = new JLabel("PW Confirm");
        PWCF_TF = new JPasswordField(10);
        CONFIRM_BT = new JButton("Sign Up");
        SIGN_MODE_BT = new JButton("CLOSE");
        SIGN_MODE_BT.setVisible(false);

        WARNING_LB = new JLabel("");

        SIGNUP_P.add(ID_LB);
        SIGNUP_P.add(ID_TF);
        SIGNUP_P.add(PW_LB);
        SIGNUP_P.add(PW_TF);
        SIGNUP_P.add(PWCF_LB);
        SIGNUP_P.add(PWCF_TF);
        SIGNUP_P.add(SIGN_MODE_BT);
        SIGNUP_P.add(CONFIRM_BT);


        CP.add(SIGNUP_P);
        CP.add(WARNING_LB);

        setVisible(false);
    }

}
