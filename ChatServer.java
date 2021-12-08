package chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;

class Server_DATA {
    final Socket Client_sock;
    String USER_ID = "";
    boolean login_NOW = false;
    ObjectOutputStream toClient_Obj;

    Server_DATA(Socket sock) throws IOException {
        this.Client_sock = sock;
        toClient_Obj = new ObjectOutputStream(Client_sock.getOutputStream());
    }
}

public class ChatServer extends Thread {
    Server_DATA Client_DATA;
    private final Socket sock;
    private static final ArrayList<String> Particiants = new ArrayList<>(10); //클라이언트 이름 담는 배열 (공개)
    private static final ArrayList<Server_DATA> Connected_Clients = new ArrayList<>(10); //클라이언트 소켓을 담는 배열 (비공개)

    private static final int SALT_SIZE = 16;
    private static final jdbc db = new jdbc();

    public ChatServer(Socket sock) {
        this.sock = sock;
    }

//    public void remove(Socket socket) {
//        for (Server_DATA d : ChatServer.Connected_Clients) {
//            synchronized (sock) {
//                if (socket == d.Client_sock) {
//                    ChatServer.Connected_Clients.remove(d);
//                    break;
//                }
//            }
//        }
//    }

    public void ECHO_CONNECT(String ID_, boolean connect_) throws IOException {
        Server_DATA temp_DATA = null;
        synchronized (ChatServer.Connected_Clients) {
            synchronized (ChatServer.Particiants) {
                for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열을 반복
                    if (sock != d.Client_sock) { //보낸 클라이언트를 제외하는 부분
                        d.toClient_Obj.writeObject(new login_users(ID_, connect_, ChatServer.Particiants));
                        d.toClient_Obj.flush();
                    } else {
                        temp_DATA = d;
                    }
                }
                if (!connect_) {
                    if (!Particiants.remove(ID_))
                        System.out.println(ID_ + " : P remove fail");
                    if (!Connected_Clients.remove(temp_DATA))
                        System.out.println(temp_DATA + " : CC remove fail");
                }
            }
        }
    }

    public void run() {
        //쓰레드가 할 일
        ObjectInputStream fromClient_Obj;
        String temp_string = "";

        try {
            System.out.println(sock + ": 연결됨");
            this.Client_DATA = new Server_DATA(sock);
            fromClient_Obj = new ObjectInputStream(sock.getInputStream()); //InputStream의 최종 형식을 Object로 설정해줍니다.

            while (true) { //채팅 수신을 기다리는 부분
                Object temp_Object = fromClient_Obj.readObject(); //Socket로부터 받은 데이터를 Object로 수신합니다.
                if (temp_Object instanceof user_) { //전달받은 객체가 사용자 타입일때
                    user_ temp_USER = (user_) temp_Object;

                    if (temp_USER.login_ == 0) { //클라이언트의 로그인 시도
                        if (get_User(temp_USER.ID_, temp_USER.PW_.getBytes())) {

                            synchronized (ChatServer.Connected_Clients) {
                                if (ChatServer.Connected_Clients.size() >= 10) {
                                    Client_DATA.toClient_Obj.writeObject(new command(1, false, "서버의 정원이 가득 찼습니다."));
                                    Client_DATA.toClient_Obj.flush();
                                    System.out.println(sock + " : 수용인원 초과로 로그인 실패");
                                    continue;
                                }
                                Client_DATA.login_NOW = true;
                                Client_DATA.USER_ID = temp_USER.ID_;
                                ChatServer.Connected_Clients.add(Client_DATA);
                                Client_DATA.toClient_Obj.writeObject(new command(1, true, temp_USER.ID_));
                                Client_DATA.toClient_Obj.flush();
                            }
                            synchronized (ChatServer.Particiants) {
                                ChatServer.Particiants.add(temp_USER.ID_);
                                Client_DATA.toClient_Obj.writeObject(new login_users(temp_USER.ID_, true, ChatServer.Particiants));
                                Client_DATA.toClient_Obj.flush();
                            }

                            ECHO_CONNECT(Client_DATA.USER_ID, true);

                            System.out.println(sock + " : 로그인 성공");
                        } else {
                            Client_DATA.toClient_Obj.writeObject(new command(1, false, "ID 또는 PW를 확인해 주세요."));
                            System.out.println(sock + " : 로그인 실패");
                        }
                    } else if (temp_USER.login_ == 2) { //클라이언트의 등록 시도
                        temp_string = set_User(temp_USER.ID_, temp_USER.PW_.getBytes());

                        if (temp_string == "") {
                            Client_DATA.toClient_Obj.writeObject(new command(2, true, "등록 성공"));
                            System.out.println(sock + " : 등록 성공");
                        } else {
                            Client_DATA.toClient_Obj.writeObject(new command(2, false, temp_string));
                            System.out.println(sock + " : " + temp_string);
                        }
                    }
                } else if (temp_Object instanceof chat_) { //전달받은 객체가 채팅타입일 때
                    chat_ temp_CHAT = (chat_) temp_Object;
                    System.out.print(sock);
                    System.out.println(temp_CHAT);

                    if (temp_CHAT.SILENT.compareTo("") == 0) {
                        synchronized (Connected_Clients) {
                            db.Insert_chat(temp_CHAT.ID_, temp_CHAT.chat_TEXT_, temp_CHAT.upload_TIME_, temp_CHAT.SILENT);
                            for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열을 반복
                                if (sock != d.Client_sock) { //보낸 클라이언트를 제외하는 부분
                                    d.toClient_Obj.writeObject(temp_CHAT);
                                    d.toClient_Obj.flush();
                                }
                            }
                        }
                    } else if (temp_CHAT.SILENT.compareTo("/save_all_chat") == 0) {
                        Client_DATA.toClient_Obj.writeObject(new command(5, true, db.Select_All("chat")));
                    } else if (temp_CHAT.SILENT.compareTo("/save_all_user") == 0) {
                        Client_DATA.toClient_Obj.writeObject(new command(6, true, db.Select_All("user")));
                    } else {//귓속말 상대 배열 반복
                        boolean isntThere = true;
                        synchronized (Connected_Clients) {
                            for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열 반복
                                if (sock != d.Client_sock && temp_CHAT.SILENT.compareTo(d.USER_ID) == 0) { //보낸 클라이언트를 제외하고 귓속말 상대를 찾는 부분
                                    d.toClient_Obj.writeObject(temp_CHAT);
                                    d.toClient_Obj.flush();
                                    isntThere = false;
                                    break;
                                }
                            }
                        }
                        if (isntThere)
                            Client_DATA.toClient_Obj.writeObject(new chat_("SERVER ALERT", "상대방을 서버에서 찾을 수 없습니다.", "", Client_DATA.USER_ID));
                        else
                            db.Insert_chat(temp_CHAT.ID_, temp_CHAT.chat_TEXT_, temp_CHAT.upload_TIME_, temp_CHAT.SILENT);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(sock + ": 연결 끊김 접속 오류 (" + ex + ")");
        } catch (Exception ex) {
            System.out.println(sock + ": 연결 끊김 (" + ex + ")");
        } finally {
            try {
                if (Client_DATA.login_NOW) {
                    ECHO_CONNECT(Client_DATA.USER_ID, false);
                }
                if (sock != null) { //클라이언트가 접속을 종료했을때 소켓을 지우는 부분
//                    remove(sock);
                    sock.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        ServerSocket serverSock = new ServerSocket(8888);
        System.out.println(serverSock + ": 서버 소켓 생성");

//        jdbc dele = new jdbc(); //데이터베이스 청소
//        dele.Delete_ALL_chat();
//        dele.Delete_ALL_user();

        while (true) { //클라이언트의 접속을 대기하는 부분
            if (Connected_Clients.size() < 10) {
                ChatServer myServer = new ChatServer(serverSock.accept());
                myServer.start();
            }
        }
    }

    // 새로운 계정 만들기
    String set_User(String ID_, byte[] Password) throws Exception {
        String SALT = getSALT();
        return db.Insert_user(ID_, Hashing(Password, SALT), SALT);
    }

    // 유저 정보와 대조한 뒤 로그인 하기
    boolean get_User(String ID_, byte[] password_) throws Exception {
        String temp_salt = db.get_SALT(ID_); // 해당 ID의 SALT 값을 찾는다
        String temp_pass = Hashing(password_, temp_salt); // 얻어온 Salt 와 password 를 조합해본다.

        return db.check(ID_, temp_pass); // db 에 저장된 아이디와 비밀번호를 대조한
    }

    // 비밀번호 해싱
    private String Hashing(byte[] password_, String Salt) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");    // SHA-256 해시함수를 사용

        // key-stretching
        for (int i = 0; i < 10; i++) {
            String temp = Byte_to_String(password_) + Salt;    // 패스워드와 Salt 를 합쳐 새로운 문자열 생성
            md.update(temp.getBytes());                        // temp 의 문자열을 해싱하여 md 에 저장해둔다
            password_ = md.digest();                            // md 객체의 다이제스트를 얻어 password 를 갱신한다
        }
        return Byte_to_String(password_);
    }

    // SALT 값 생성
    private String getSALT() throws Exception {
        SecureRandom rnd = new SecureRandom();
        byte[] temp = new byte[SALT_SIZE];
        rnd.nextBytes(temp);

        return Byte_to_String(temp);
    }

    // 바이트 값을 16진수로 변경해준다
    private String Byte_to_String(byte[] temp) {
        StringBuilder sb = new StringBuilder();
        for (byte a : temp) {
            sb.append(String.format("%02x", a));
        }
        return sb.toString();
    }
}
