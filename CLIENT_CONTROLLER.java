package chat;

import javafx.application.Platform;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;

public class CLIENT_CONTROLLER {
    SIGNIN_GUI SIGN_IN_VIEW;
    SIGNUP_GUI SIGN_UP_VIEW;

    private boolean LOGIN_NOW = false;
    private boolean register_mode = false;

    Socket sock;
    ObjectOutputStream toServer_Obj;
    ObjectInputStream fromServer_OBJ;

    String USER_ID = "";

    LinkedList<chat_> CHAT_LIST = new LinkedList();
    ArrayList<String> Participant = new ArrayList<>();

    CLIENT_CONTROLLER(){
        SIGN_IN_VIEW = new SIGNIN_GUI();
        SIGN_UP_VIEW = new SIGNUP_GUI();
        SIGN_IN_VIEW.CONFIRM_BT.addActionListener(this::Sign_IN_Action);
        SIGN_IN_VIEW.SIGN_MODE_BT.addActionListener(this::Sign_IN_Action);
        SIGN_UP_VIEW.CONFIRM_BT.addActionListener(this::Sign_UP_Action);

        try {
            sock = new Socket("localhost", 8888);
            toServer_Obj = new ObjectOutputStream(sock.getOutputStream());
            fromServer_OBJ = new ObjectInputStream(sock.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread From_Server = new Thread(() -> {
            try {
                while (true) { //수신을 기다리는 부분 (스트림이 종료되면 -1이 됨)
                    Object temp_Object = (Object) fromServer_OBJ.readObject(); //Socket로부터 받은 데이터를 Object로 수신합니다.
                    if (temp_Object instanceof command) { //전달받은 객체가 명령어 타입일때
                        get_command_((command) temp_Object);
                    } else if (temp_Object instanceof chat_) { //전달받은 객체가 채팅타입일 때
//                        get_chat_((chat_) temp_Object);
//                    } else if (temp_Object instanceof login_users) {
//                        get_login_users_((login_users) temp_Object);
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {

                System.out.println("연결 종료 (" + ex + ")");
            } finally {
                try {
                    if (sock != null)
                        sock.close();
                } catch (IOException ex) {
                }
            }
        });
        From_Server.setDaemon(true);
        From_Server.start();

    }

    private void get_command_(command temp_COMMAND) throws IOException {
        if (temp_COMMAND.command_type == 1) { //클라이언트의 로그인에 대한 응답
            if (temp_COMMAND.state) {
                USER_ID = temp_COMMAND.message;
                LOGIN_NOW = true;
//                CHANGE_SCENE();
            } else {
                SIGN_IN_VIEW.WARNING_LB.setText(temp_COMMAND.message);
            }
        } else if (temp_COMMAND.command_type == 2) { //클라이언트의 등록에 대한 응답
            if (temp_COMMAND.state) {
                SIGN_UP_VIEW.WARNING_LB.setText("가입에 성공하였습니다.");
            } else {
                SIGN_UP_VIEW.WARNING_LB.setText(temp_COMMAND.message);
            }
        } else if (temp_COMMAND.command_type == 5) {
//            SAVE_ALL_("chat", temp_COMMAND.message);
        } else if (temp_COMMAND.command_type == 6) {
//            SAVE_ALL_("user", temp_COMMAND.message);
        }
    }

//    private void get_chat_(chat_ temp_CHAT) {
//        synchronized (CHAT_LIST) {
//            CHAT_LIST.addFirst(temp_CHAT);
//        }
//        if (temp_CHAT.ID_.compareTo("SERVER ALERT") == 0)
//            create_CHAT_BOX_(0, temp_CHAT.ID_ + " | " + temp_CHAT.upload_TIME_, temp_CHAT.chat_TEXT_, false);
//        else if (temp_CHAT.SILENT.compareTo("") == 0)
//            create_CHAT_BOX_(2, temp_CHAT.ID_ + " | " + temp_CHAT.upload_TIME_, temp_CHAT.chat_TEXT_, false);
//        else
//            create_CHAT_BOX_(2, temp_CHAT.ID_ + " | " + temp_CHAT.upload_TIME_, temp_CHAT.chat_TEXT_, true);
//    }

/*    private void get_login_users_(login_users temp_LOGIN_USERS){
        String alert_message = "";

        if (temp_LOGIN_USERS.ID_.compareTo(USER_ID) == 0){
            alert_message = temp_LOGIN_USERS.ID_ + "님 채팅에 오신것을 환영합니다.";
            Participant = (ArrayList<String>) temp_LOGIN_USERS.users_ID_.clone();
        } else {
            if (temp_LOGIN_USERS.state) {
                Participant.add(temp_LOGIN_USERS.ID_);
                alert_message = temp_LOGIN_USERS.ID_ + "님이 참가했습니다.";
            } else {
                Participant.remove(temp_LOGIN_USERS.ID_);
                alert_message = temp_LOGIN_USERS.ID_ + "님이 퇴장했습니다.";
            }
        }
        String finalAlert_message = alert_message;
//        create_CHAT_BOX_(0, "SEVER ALERT", finalAlert_message, false);
//        PARTY_MSG.setText(temp_LOGIN_USERS.toString());
        synchronized (CHAT_LIST) {
            CHAT_LIST.addFirst(new chat_("SERVER ALERT", temp_LOGIN_USERS.toString(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))));
        }
    }*/

    public void Sign_IN_Action(ActionEvent e) {
        try {
            if (e.getSource()== SIGN_IN_VIEW.CONFIRM_BT){ //로그인 창 확인 버튼
                if (SIGN_IN_VIEW.ID_TF.getText().length() == 0) {
                    SIGN_IN_VIEW.WARNING_LB.setText("ID를 입력해주십시오.");
                } else if (SIGN_IN_VIEW.PW_TF.getText().length() == 0) {
                    SIGN_IN_VIEW.WARNING_LB.setText("PW를 입력해주십시오.");
                } else {
                    toServer_Obj.writeObject(new user_(SIGN_IN_VIEW.ID_TF.getText(), SIGN_IN_VIEW.PW_TF.getText(), 0).clone());
                    toServer_Obj.flush();
                }
            } else if (e.getSource()== SIGN_IN_VIEW.SIGN_MODE_BT){ //로그인 창 가입 버튼
                SIGN_UP_VIEW.setVisible(true);
            }
        } catch (IOException | NoSuchAlgorithmException ex){
            SIGN_IN_VIEW.setTitle("Lost Connection");
            SIGN_UP_VIEW.setTitle("Lost Connection");

        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
    }

    public void Sign_UP_Action(ActionEvent e){
        try{
            if (e.getSource()== SIGN_UP_VIEW.CONFIRM_BT){ //가입 창 확인 버튼
                if (SIGN_UP_VIEW.ID_TF.getText().length() == 0) {
                    SIGN_UP_VIEW.WARNING_LB.setText("ID를 입력해주십시오.");
                } else if (SIGN_UP_VIEW.PW_TF.getText().length() == 0) {
                    SIGN_UP_VIEW.WARNING_LB.setText("PW를 입력해주십시오.");
                } else if (SIGN_UP_VIEW.PWCF_TF.getText().length() == 0) {
                    SIGN_UP_VIEW.WARNING_LB.setText("PW Confirm을 입력해주십시오.");
                } else if (SIGN_UP_VIEW.PW_TF.getText().compareTo(SIGN_UP_VIEW.PWCF_TF.getText()) != 0) {
                    SIGN_UP_VIEW.WARNING_LB.setText("PW와 PW Confirm이 일치하지 않습니다.");
                } else {
                    SIGN_UP_VIEW.WARNING_LB.setText("");
                    toServer_Obj.writeObject(new user_(SIGN_UP_VIEW.ID_TF.getText(), SIGN_UP_VIEW.PW_TF.getText(), 2));
                    toServer_Obj.flush();
                }
            }
        }catch (IOException | NoSuchAlgorithmException ex){
            SIGN_IN_VIEW.setTitle("Lost Connection");
            SIGN_UP_VIEW.setTitle("Lost Connection");
//        } catch (CloneNotSupportedException ex) {
//            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CLIENT_CONTROLLER();
    }
}
