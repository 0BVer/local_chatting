package chat;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CLIENT_CONTROLLER {
    SIGNIN_GUI SIGN_IN_VIEW;
    SIGNUP_GUI SIGN_UP_VIEW;
    CHAT_GUI CHAT_VIEW;
    POSTNUM_API_GUI POSTNUM_VIEW;

    POSTNUM_API postnum_API;

    private boolean LOGIN_NOW = false;
    private boolean register_mode = false;

    Socket sock;
    ObjectOutputStream toServer_Obj;
    ObjectInputStream fromServer_OBJ;

    String ID_ = "";

    user_DATA MY_DATA;
    LinkedList<chat_> CHAT_LIST = new LinkedList();

    CLIENT_CONTROLLER() {
        SIGN_IN_VIEW = new SIGNIN_GUI();
        SIGN_UP_VIEW = new SIGNUP_GUI();
        CHAT_VIEW = new CHAT_GUI();
        POSTNUM_VIEW = new POSTNUM_API_GUI();

        SIGN_IN_VIEW.CONFIRM_BT.addActionListener(this::Sign_IN_Action);
        SIGN_IN_VIEW.SIGN_MODE_BT.addActionListener(this::Sign_IN_Action);

        SIGN_UP_VIEW.CONFIRM_BT.addActionListener(this::Sign_UP_Action);
        SIGN_UP_VIEW.PN_BT.addActionListener(this::Sign_UP_Action);

        POSTNUM_VIEW.SEARCH_BT.addActionListener(this::PN_Search_Action);
        POSTNUM_VIEW.PREV_BT.addActionListener(this::PN_Search_Action);
        POSTNUM_VIEW.NEXT_BT.addActionListener(this::PN_Search_Action);
        for (JButton BT : POSTNUM_VIEW.PN_BT) {
            BT.addActionListener(this::PN_Search_Action);
        }

        CHAT_VIEW.SEND_BT.addActionListener(this::Send_Action);

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
                        get_chat_((chat_) temp_Object);
//                    } else if (temp_Object instanceof login_users) {
//                        get_login_users_((login_users) temp_Object);
                    } else if (temp_Object instanceof user_DATA) { //전달받은 객체가 명령어 타입일때
                        get_user_DATA_((user_DATA) temp_Object);
                    }
                }
            } catch (IOException | ClassNotFoundException | CloneNotSupportedException ex) {
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
            System.out.println(temp_COMMAND.message);
            if (temp_COMMAND.state) {
                this.ID_ = temp_COMMAND.message;
                LOGIN_NOW = true;
                SIGN_IN_VIEW.setVisible(false);
                CHAT_VIEW.setVisible(true);

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

    private void get_user_DATA_(user_DATA temp_USER_DATA) throws CloneNotSupportedException {
        if (this.ID_ == temp_USER_DATA.ID_) {
            MY_DATA = temp_USER_DATA.clone();
        } else {
            Participant.add(temp_USER_DATA.clone());
        }
    }

    private void get_chat_(chat_ temp_CHAT) {
        synchronized (CHAT_LIST) {
            CHAT_LIST.addFirst(temp_CHAT);
        }
        CHAT_VIEW.CHATBOX_TA.append(temp_CHAT.SENDER_INDEX + " -> " + temp_CHAT.RECEIVER_INDEX + " : " + temp_CHAT.chat_TEXT_ + "\n");
    }

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
            if (e.getSource() == SIGN_IN_VIEW.CONFIRM_BT) { //로그인 창 확인 버튼
                if (SIGN_IN_VIEW.ID_TF.getText().length() == 0) {
                    SIGN_IN_VIEW.WARNING_LB.setText("ID를 입력해주십시오.");
                } else if (SIGN_IN_VIEW.PW_TF.getText().length() == 0) {
                    SIGN_IN_VIEW.WARNING_LB.setText("PW를 입력해주십시오.");
                } else {
                    toServer_Obj.writeObject(new user_SIGN(SIGN_IN_VIEW.ID_TF.getText(), SIGN_IN_VIEW.PW_TF.getText(), 0).clone());
                    toServer_Obj.flush();
                }
            } else if (e.getSource() == SIGN_IN_VIEW.SIGN_MODE_BT) { //로그인 창 가입 버튼
                SIGN_UP_VIEW.setVisible(true);
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            SIGN_IN_VIEW.setTitle("Lost Connection");
            SIGN_UP_VIEW.setTitle("Lost Connection");

        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
    }

    public void Sign_UP_Action(ActionEvent e) {
        try {
            if (e.getSource() == SIGN_UP_VIEW.CONFIRM_BT) { //가입 창 확인 버튼
                if (SIGN_UP_VIEW.ID_TF.getText().length() == 0) {
                    SIGN_UP_VIEW.WARNING_LB.setText("ID를 입력해주십시오.");
                } else if (SIGN_UP_VIEW.PW_TF.getText().length() == 0) {
                    SIGN_UP_VIEW.WARNING_LB.setText("PW를 입력해주십시오.");
                } else if (SIGN_UP_VIEW.PWCF_TF.getText().length() == 0) {
                    SIGN_UP_VIEW.WARNING_LB.setText("PW Confirm을 입력해주십시오.");
                } else if (SIGN_UP_VIEW.PW_TF.getText().compareTo(SIGN_UP_VIEW.PWCF_TF.getText()) != 0) {
                    SIGN_UP_VIEW.WARNING_LB.setText("PW와 PW Confirm이 일치하지 않습니다.");
                } else if (SIGN_UP_VIEW.PN_RESULT_LB.getText().length() == 0) { //TODO: 우편번호 전송 및 처리 추가
                    SIGN_UP_VIEW.WARNING_LB.setText("우편번호를 입력해주십시오.");
                } else {
                    SIGN_UP_VIEW.WARNING_LB.setText("");
                    toServer_Obj.writeObject(new user_SIGN(SIGN_UP_VIEW.ID_TF.getText(), SIGN_UP_VIEW.PW_TF.getText(), 2));
                    toServer_Obj.flush();
                }
            } else if (e.getSource() == SIGN_UP_VIEW.PN_BT) {
                POSTNUM_VIEW.setVisible(true);
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            SIGN_IN_VIEW.setTitle("Lost Connection");
            SIGN_UP_VIEW.setTitle("Lost Connection");
//        } catch (CloneNotSupportedException ex) {
//            ex.printStackTrace();
        }
    }

    private void PN_Search_Action(ActionEvent e) {
        if (e.getSource() == POSTNUM_VIEW.SEARCH_BT) {
            if (POSTNUM_VIEW.SEARCH_TF.getText().length() == 0) {
                POSTNUM_VIEW.WARNING_LB.setText("주소를 입력해주십시오.");
            } else {
                POSTNUM_SEARCH();
            }
        } else if (e.getSource() == POSTNUM_VIEW.NEXT_BT){
            POSTNUM_VIEW.searchPage++;
            POSTNUM_SEARCH();
        } else if (e.getSource() == POSTNUM_VIEW.PREV_BT){
            POSTNUM_VIEW.searchPage--;
            POSTNUM_SEARCH();
        } else if (POSTNUM_VIEW.PN_BT.contains(e.getSource())){
            int temp = POSTNUM_VIEW.PN_BT.indexOf(e.getSource());
            SIGN_UP_VIEW.PN_RESULT_LB.setText(String.valueOf(POSTNUM_VIEW.PN_LB.get(temp).getText()));
            SIGN_UP_VIEW.PN_RESULT_LB.setToolTipText(String.valueOf(POSTNUM_VIEW.PN_DETAIL_LB.get(temp*2).getText()));
            POSTNUM_VIEW.setVisible(false);
        }
    }

    private void POSTNUM_SEARCH(){
        List<String> RESULT = new ArrayList<>(10);
        int[] RESULT_COUNT = new int[2];
        postnum_API = new POSTNUM_API(POSTNUM_VIEW.SEARCH_TF.getText(), POSTNUM_VIEW.searchPage, RESULT, RESULT_COUNT);

        for (int i = 0; i < 10; i++){
            POSTNUM_VIEW.PN_LB.get(i).setVisible(false);
            POSTNUM_VIEW.PN_DETAIL_LB.get(i*2+0).setVisible(false);
            POSTNUM_VIEW.PN_DETAIL_LB.get(i*2+1).setVisible(false);
            POSTNUM_VIEW.PN_BT.get(i).setVisible(false);
        }

        if (RESULT_COUNT[0] == 0){
            POSTNUM_VIEW.WARNING_LB.setText("검색 결과가 없습니다.");
            return;
        } else if (RESULT_COUNT[0] <= 10){
            POSTNUM_VIEW.NEXT_BT.setVisible(false);
            POSTNUM_VIEW.PREV_BT.setVisible(false);
        } else if (RESULT_COUNT[0] > POSTNUM_VIEW.searchPage * 10 && POSTNUM_VIEW.searchPage == 1){
            POSTNUM_VIEW.NEXT_BT.setVisible(true);
            POSTNUM_VIEW.PREV_BT.setVisible(false);
        } else if (RESULT_COUNT[0] <= POSTNUM_VIEW.searchPage * 10 && POSTNUM_VIEW.searchPage != 1){
            POSTNUM_VIEW.NEXT_BT.setVisible(false);
            POSTNUM_VIEW.PREV_BT.setVisible(true);
        } else {
            POSTNUM_VIEW.NEXT_BT.setVisible(true);
            POSTNUM_VIEW.PREV_BT.setVisible(true);
        }

        for (int i = 0; i < RESULT.size()/3; i++){
            POSTNUM_VIEW.PN_LB.get(i).setText(RESULT.get(i*3+0));
            POSTNUM_VIEW.PN_DETAIL_LB.get(i*2).setText(RESULT.get(i*3+1));
            POSTNUM_VIEW.PN_DETAIL_LB.get(i*2+1).setText(RESULT.get(i*3+2));
            POSTNUM_VIEW.PN_LB.get(i).setVisible(true);
            POSTNUM_VIEW.PN_DETAIL_LB.get(i*2+0).setVisible(true);
            POSTNUM_VIEW.PN_DETAIL_LB.get(i*2+1).setVisible(true);
            POSTNUM_VIEW.PN_BT.get(i).setVisible(true);
        }

        POSTNUM_VIEW.WARNING_LB.setText("");
    }

    private void Send_Action(ActionEvent actionEvent) {
        try {
            //        String time_;
            if (CHAT_VIEW.CHAT_TF.getText().length() > 0) {
//            time_ = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
                int reseiver_INDEX = 55;
                boolean ISIT_group = false;
                chat_ temp_chat = null;
                toServer_Obj.writeObject(temp_chat = new chat_(MY_DATA.INDEX_, reseiver_INDEX, ISIT_group, CHAT_VIEW.CHAT_TF.getText()));
                toServer_Obj.flush();
                CHAT_LIST.addFirst(temp_chat);
                CHAT_VIEW.CHATBOX_TA.append(MY_DATA.INDEX_ + " -> " + reseiver_INDEX + " : " + CHAT_VIEW.CHAT_TF.getText() + "\n");
                CHAT_VIEW.CHAT_TF.setText("");
            }
        } catch (IOException ex) {

        }
    }

    Set<user_DATA> Participant = new Set<user_DATA>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<user_DATA> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(user_DATA user_data) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends user_DATA> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    };

    public static void main(String[] args) {
        new CLIENT_CONTROLLER();
    }
}
