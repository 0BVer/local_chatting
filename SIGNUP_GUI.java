package chat;

import javax.swing.*;
import java.awt.*;

public class SIGNUP_GUI extends JFrame {
    Container CP;
    JPanel SIGNUP_P, PN_SEARCH_P;
    JLabel ID_LB, PW_LB, PWCF_LB, PN_TITLE_LB, PN_RESULT_LB, WARNING_LB;
    JTextField ID_TF, PW_TF, PWCF_TF;
    JButton PN_BT, CONFIRM_BT, SIGN_MODE_BT;

    SIGNUP_GUI() {
        CP = getContentPane();
        setTitle("Sign UP");
        setSize(400, 500);
        CP.setLayout(new FlowLayout());

        SIGNUP_P = new JPanel(new GridLayout(5, 2, 5, 5));
        SIGNUP_P.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ID_LB = new JLabel("ID");
        ID_TF = new JTextField(10);
        PW_LB = new JLabel("PW");
        PW_TF = new JPasswordField(10);
        PWCF_LB = new JLabel("PW Confirm");
        PWCF_TF = new JPasswordField(10);


        PN_TITLE_LB = new JLabel("POST Num");
        PN_RESULT_LB = new JLabel("");
        PN_BT = new JButton("\uD83D\uDD0D");

        PN_SEARCH_P = new JPanel(new GridLayout(1, 2));
        PN_SEARCH_P.add(PN_RESULT_LB);
        PN_SEARCH_P.add(PN_BT);

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
        SIGNUP_P.add(PN_TITLE_LB);
        SIGNUP_P.add(PN_SEARCH_P);
        SIGNUP_P.add(SIGN_MODE_BT);
        SIGNUP_P.add(CONFIRM_BT);

        CP.add(SIGNUP_P);
        CP.add(WARNING_LB);

        setVisible(false);
    }
}
