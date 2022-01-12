package chat;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class command implements Serializable, Cloneable{
    int command_type; //1 : 로그인 시도, 2 : 가입 시도, 11 : 사용자 정보 요청
    boolean state;
    String message;

    command(int command_type, boolean state, String message) {
        this.command_type = command_type;
        this.state = state;
        this.message = message;
    }
}

class chat_ implements Serializable {
    user_DATA SENDER;
    user_DATA RECEIVER;
    boolean ISIT_group = false;
    String chat_TEXT_ = "";

    chat_(user_DATA SENDER, user_DATA RECEIVER, boolean ISIT_group, String chat_TEXT_){
        this.SENDER = SENDER;
        this.RECEIVER = RECEIVER;
        this.ISIT_group = ISIT_group;
        this.chat_TEXT_ = chat_TEXT_;
    }
}

class user_DATA implements Serializable, Cloneable{
    int INDEX_ = 0;
    String ID_ = "";
    String NN_ = "";

    user_DATA(int INDEX_, String ID_, String NN_){
        this.INDEX_ = INDEX_;
        this.ID_ = ID_;
        this.NN_ = NN_;
    }

    protected user_DATA clone() throws CloneNotSupportedException {
        return (user_DATA) super.clone();
    }
}

class user_SIGN implements Serializable, Cloneable {
    String ID_ = "";
    String PW_ = "";
    int login_ = 0; //0:로그인 시도중, 1:로그인 완료, 2:등록

    user_SIGN(String ID_, String PW_, int login_) throws NoSuchAlgorithmException {
        if (login_ == 0 || login_ == 2){
            this.ID_ = ID_;
            setPW_(PW_);
            this.login_ = login_;
        }
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

    protected user_SIGN clone() throws CloneNotSupportedException {
        return (user_SIGN) super.clone();
    }
}
