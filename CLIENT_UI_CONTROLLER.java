package chat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

public class CLIENT_UI_CONTROLLER implements Initializable {
    public Button REG_BT;
    public Button LOGIN_BT;
    public Text WARNING_MSG;
    @FXML
    TextField TXT_ID;
    @FXML
    private TextField TXT_PW;
    @FXML
    private TextField TXT_PW_CF;
    private boolean register_mode = false;
    Socket sock;
    ObjectOutputStream toServer_Obj;

    command temp_COMMAND;
    chat_ temp_CHAT;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            sock = new Socket("localhost", 8888);
            ServerHandler_v2 chandler = new ServerHandler_v2(sock);
            toServer_Obj = new ObjectOutputStream(sock.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread fromServer = new Thread() {
            @Override
            public void run() {
                InputStream fromServer = null;
                ObjectInputStream fromServer_OBJ = null;
                try {
                    fromServer = sock.getInputStream();
                    fromServer_OBJ = new ObjectInputStream(fromServer);

                    while (true) { //채팅 수신을 기다리는 부분 (스트림이 종료되면 -1이 됨)
                        Object temp_Object = (Object) fromServer_OBJ.readObject(); //Socket로부터 받은 데이터를 Object로 수신합니다.
                        if (temp_Object instanceof command) { //전달받은 객체가 사용자 타입일때
                            temp_COMMAND = (command) temp_Object;
                            System.out.println(temp_COMMAND.message);
                            if (!temp_COMMAND.state) {
                                if (temp_COMMAND.command_type == 1) { //클라이언트의 로그인에 대한 응답
                                    WARNING_MSG.setText(temp_COMMAND.message);
                                } else if (temp_COMMAND.command_type == 2) { //클라이언트의 등록에 대한 응답
                                    WARNING_MSG.setText(temp_COMMAND.message);
                                }
                            }
                        } else if (temp_Object instanceof chat_) { //전달받은 객체가 채팅타입일 때
                            temp_CHAT = (chat_) temp_Object;
                            System.out.println(temp_CHAT);
                        }
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    System.out.println("연결 종료 (" + ex + ")");
                } finally {
                    try {
                        if (fromServer != null) {
                            fromServer.close();
                            fromServer_OBJ.close();
                        }
                        if (sock != null)
                            sock.close();
                    } catch (IOException ex) {
                    }
                }
            }
        };
        fromServer.setDaemon(true);
        fromServer.start();
    }

    public void REGISTER_IN(ActionEvent actionEvent) {
        if (!register_mode) {
            TXT_PW_CF.setVisible(true);
            register_mode = true;
            REG_BT.setText("처음이 아닌가요?");
            LOGIN_BT.setText("가입하기");
            WARNING_MSG.setText("");
        } else {
            TXT_PW_CF.setVisible(false);
            TXT_PW_CF.setText("");
            register_mode = false;
            REG_BT.setText("처음이신가요?");
            LOGIN_BT.setText("로그인하기");
            WARNING_MSG.setText("");
        }
    }

    public void LOGIN_IN(ActionEvent actionEvent) throws NoSuchAlgorithmException, IOException {
        if (!register_mode) {
            if (TXT_ID.getText().length() == 0) {
                WARNING_MSG.setText("ID를 입력해주십시오.");
            } else if (TXT_PW.getText().length() == 0) {
                WARNING_MSG.setText("PW를 입력해주십시오.");
            } else {
                toServer_Obj.writeObject(new user_(TXT_ID.getText(), TXT_PW.getText(), 0));
            }
        } else {
            if (TXT_ID.getText().length() == 0) {
                WARNING_MSG.setText("ID를 입력해주십시오.");
            } else if (TXT_PW.getText().length() == 0) {
                WARNING_MSG.setText("PW를 입력해주십시오.");
            } else if (TXT_PW_CF.getText().length() == 0) {
                WARNING_MSG.setText("PW Confirm를 입력해주십시오.");
            } else if (TXT_PW.getText().compareTo(TXT_PW_CF.getText()) != 0) {
                WARNING_MSG.setText("PW와 PW Confirm이 일치하지 않습니다.");
            } else {
                toServer_Obj.writeObject(new user_(TXT_ID.getText(), TXT_PW.getText(), 2));
            }
        }
    }
}
