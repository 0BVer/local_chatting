package chat;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class command implements Serializable{
    int command_type;
    boolean state;
    String message;
    command(int command_type, boolean state, String message){
        this.command_type = command_type;
        this.state = state;
        this.message = message;
    }
}

class chat_ implements Serializable {
    String ID_ = "unknown";
    String chat_TEXT_ = "";
    String upload_TIME_ = "";

    chat_(String ID_, String chat_TEXT_){
        this.ID_ = ID_;
        this.chat_TEXT_ = chat_TEXT_;
    }

    chat_(String ID_, String chat_TEXT_, String upload_TIME_){
        this.ID_ = ID_;
        this.chat_TEXT_ = chat_TEXT_;
        this.upload_TIME_ = upload_TIME_;
    }

    @Override
    public String toString() {
        return "chat_{" +
                "ID_='" + ID_ + '\'' +
                ", chat_TEXT_='" + chat_TEXT_ + '\'' +
                ", upload_TIME_='" + upload_TIME_ + '\'' +
                '}';
    }
}

class user_ implements Serializable {
    String ID_ = "";
    String PW_ = "";
    int login_ = 0; //0:로그인 시도중, 1:로그인 완료, 2:등록

    public void setPW_(byte[] password_) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");    // SHA-256 해시함수를 사용

        // key-stretching
        for (int i = 0; i < 10000; i++) {
            String temp = Byte_to_String(password_);
            md.update(temp.getBytes());                        // temp 의 문자열을 해싱하여 md 에 저장해둔다
            password_ = md.digest();                            // md 객체의 다이제스트를 얻어 password 를 갱신한다
        }
        this.PW_ = Byte_to_String(password_);
    }

    // 바이트 값을 16진수로 변경해준다
    private String Byte_to_String(byte[] temp) {
        StringBuilder sb = new StringBuilder();
        for (byte a : temp) {
            sb.append(String.format("%02x", a));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "user_{" +
                "ID_='" + ID_ + '\'' +
                ", PW_='" + PW_ + '\'' +
                ", login_=" + login_ +
                '}';
    }
}