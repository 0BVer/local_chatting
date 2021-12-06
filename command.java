package chat;

import javafx.beans.binding.Bindings;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class command implements Serializable {
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
    String ID_ = "unknown";
    String chat_TEXT_ = "";
    String upload_TIME_ = "";
    String SILENT = "";

    chat_(String ID_, String chat_TEXT_, String upload_TIME_) {
        this.ID_ = ID_;
        this.chat_TEXT_ = chat_TEXT_;
        this.upload_TIME_ = upload_TIME_;
    }

    chat_(String ID_, String chat_TEXT_, String upload_TIME_, String SILENT) {
        this.ID_ = ID_;
        this.chat_TEXT_ = chat_TEXT_;
        this.upload_TIME_ = upload_TIME_;
        this.SILENT = SILENT;
    }

    @Override
    public String toString() {
        if (SILENT.compareTo("") != 0)
            return "[" + ID_ + " -> " + SILENT +
                    " | " + chat_TEXT_ +
                    " | " + upload_TIME_ +
                    ']';
        else
            return "[" + ID_ +
                    " | " + chat_TEXT_ +
                    " | " + upload_TIME_ +
                    ']';
    }
}

class user_ implements Serializable {
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
        // key-stretching
        for (int i = 0; i < 1000; i++) {
            md.update(password_.getBytes());                        // temp 의 문자열을 해싱하여 md 에 저장해둔다
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

    @Override
    public String toString() {
        return "user_{" +
                "ID_='" + ID_ + '\'' +
                ", PW_='" + PW_ + '\'' +
                ", login_=" + login_ +
                '}';
    }
}

class login_users implements Serializable {
    String ID_ = "";
    boolean state;
    ArrayList<String> users_ID_;

    login_users(String ID_, boolean state, ArrayList users_ID_) {
        this.ID_ = ID_;
        this.state = state;
        this.users_ID_ = users_ID_;
    }

    @Override
    public String toString() {
        String temp = "";
        int count = 0;
        for (String user : users_ID_) {
            if (user.length() > 0) {
                temp += user + ", ";
                count++;
            }
        }
        return String.format("Online [%d/10] ", count) + temp.substring(0, temp.length() - 2);
    }
}