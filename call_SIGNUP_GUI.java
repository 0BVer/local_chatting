package chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class call_SIGNUP_GUI implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        new SIGNUP_GUI();
    }
}
