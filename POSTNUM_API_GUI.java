package chat;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class POSTNUM_API_GUI extends JFrame {
    Container CP;
    JPanel PN_HEADBOX_P, PN_WESTBOX_P, PN_CENTERBOX_P, PN_EASTBOX_P, PN_FOOTBOX_P;
    JLabel WARNING_LB;
    JButton SEARCH_BT, NEXT_BT, PREV_BT;
    JTextField SEARCH_TF;
    ArrayList<JLabel> PN_LB = new ArrayList<>(10);
    ArrayList<JLabel> PN_DETAIL_LB = new ArrayList<>(10);
    ArrayList<JButton> PN_BT = new ArrayList<>(10);

    int searchPage = 1;

    POSTNUM_API_GUI() {
        CP = getContentPane();
        setTitle("Post Number Search");
        setSize(600, 900);
        CP.setLayout(new BorderLayout());

        SEARCH_BT = new JButton("\uD83D\uDD0D");
        SEARCH_TF = new JTextField(10);
        SEARCH_TF.setToolTipText("도로명 주소 혹은 지번 주소를 입력해 주세요");
        SEARCH_TF.setSize(10, SEARCH_BT.getHeight());
        WARNING_LB = new JLabel("");

        PN_HEADBOX_P = new JPanel(new BorderLayout());
        PN_HEADBOX_P.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        PN_HEADBOX_P.add(SEARCH_TF, BorderLayout.CENTER);
        PN_HEADBOX_P.add(SEARCH_BT, BorderLayout.EAST);
        PN_HEADBOX_P.add(WARNING_LB, BorderLayout.SOUTH);

        PN_WESTBOX_P = new JPanel(new GridLayout(10, 1, 5, 0));
        PN_WESTBOX_P.setBorder(BorderFactory.createEmptyBorder(0 , 10 , 0 , 10));
        for (int i = 0; i<10; i++) {
            PN_LB.add(new JLabel("", JLabel.CENTER));
            PN_WESTBOX_P.add(PN_LB.get(i));
        }

        PN_CENTERBOX_P = new JPanel(new GridLayout(20, 1, 5, 20));
        PN_CENTERBOX_P.setBorder(BorderFactory.createEmptyBorder(0 , 10 , 0 , 10));
        for (int i = 0; i<20; i++) {
            PN_DETAIL_LB.add(new JLabel("", JLabel.CENTER));
            PN_CENTERBOX_P.add(PN_DETAIL_LB.get(i));
        }

        PN_EASTBOX_P = new JPanel(new GridLayout(10, 1, 5, 20));
        PN_EASTBOX_P.setBorder(BorderFactory.createEmptyBorder(0 , 10 , 0 , 10));
        for (int i = 0; i<10; i++) {
            PN_BT.add(new JButton("선택"));
            PN_BT.get(i).setVisible(false);
            PN_EASTBOX_P.add(PN_BT.get(i));
        }

        PREV_BT = new JButton(" < ");
        PREV_BT.setVisible(false);
        NEXT_BT = new JButton(" > ");
        NEXT_BT.setVisible(false);

        PN_FOOTBOX_P = new JPanel(new FlowLayout());
        PN_FOOTBOX_P.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        PN_FOOTBOX_P.add(PREV_BT);
        PN_FOOTBOX_P.add(NEXT_BT);

        CP.add(PN_HEADBOX_P, BorderLayout.NORTH);
        CP.add(PN_CENTERBOX_P, BorderLayout.CENTER);
        CP.add(PN_WESTBOX_P, BorderLayout.WEST);
        CP.add(PN_EASTBOX_P, BorderLayout.EAST);
        CP.add(PN_FOOTBOX_P, BorderLayout.SOUTH);

        setVisible(false);
    }

//    public static void main(String[] args) {
//        new POSTNUM_API_GUI();
//    }
}
