package chat;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class command implements Serializable, Cloneable{
    int command_type;
    boolean state;
    String message;

    command(int command_type, boolean state, String message) {
        this.command_type = command_type;
        this.state = state;
        this.message = message;
    }
}

class chat_ implements Serializable {
    int SENDER_INDEX;
    int RECEIVER_INDEX;
    boolean ISIT_group = false;
    String chat_TEXT_ = "";

    chat_(int SENDER_INDEX, int RECEIVER_INDEX, boolean ISIT_group, String chat_TEXT_){
        this.SENDER_INDEX = SENDER_INDEX;
        this.RECEIVER_INDEX = RECEIVER_INDEX;
        this.ISIT_group = ISIT_group;
        this.chat_TEXT_ = chat_TEXT_;
    }
}

class user_ implements Serializable, Cloneable {
    String ID_ = "";
    String PW_ = "";
    int login_ = 0; //0:로그인 시도중, 1:로그인 완료, 2:등록

    user_(String ID_, String PW_, int login_) throws NoSuchAlgorithmException {
        this.ID_ = ID_;
        setPW_(PW_);
        this.login_ = login_;
    }

    private void setPW_(String password_) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");    // SHA-256 해시함수를 사용
        byte[] temp = new byte[256];
        temp = password_.getBytes();
        // key-stretching
        for (int i = 0; i < 1000; i++) {
            md.update(temp);                        // temp 의 문자열을 해싱하여 md 에 저장해둔다
            temp = md.digest();                            // md 객체의 다이제스트를 얻어 password 를 갱신한다
        }
        this.PW_ = Byte_to_String(temp);
    }

    // 바이트 값을 16진수로 변경해준다
    private String Byte_to_String(byte[] temp) {
        StringBuilder sb = new StringBuilder();
        for (byte a : temp) {
            sb.append(String.format("%02x", a));
        }
        return sb.toString();
    }

    protected user_ clone() throws CloneNotSupportedException {
        return (user_) super.clone();
    }
}
