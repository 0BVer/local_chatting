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
    int INDEX_ = 0;
    boolean login_NOW = false;
    ObjectOutputStream toClient_Obj;

    Server_DATA(Socket sock) throws IOException {
        this.Client_sock = sock;
        toClient_Obj = new ObjectOutputStream(Client_sock.getOutputStream());
    }
}

public class ChatServer extends Thread {
    Server_DATA Client_DATA;
    user_DATA USER_DATA;
    private final Socket sock;
    private static final ArrayList<user_DATA> Particiants = new ArrayList<>(10); //클라이언트 이름 담는 배열 (공개)
    private static final ArrayList<Server_DATA> Connected_Clients = new ArrayList<>(10); //클라이언트 소켓을 담는 배열 (비공개)

    private static final int SALT_SIZE = 16;
    private static final Oracle_jdbc db = new Oracle_jdbc();

    public ChatServer(Socket sock) {
        this.sock = sock;
    }

    private static int user_INDEX = 0;

    public void ECHO_CONNECT(user_DATA USER_DATA, boolean connect_) throws IOException {
        Server_DATA temp_DATA = null;
        synchronized (ChatServer.Connected_Clients) {
            synchronized (ChatServer.Particiants) {
                if (!connect_)
                    if (!Particiants.remove(USER_DATA))
                        System.out.println(USER_DATA.INDEX_ + " : P remove fail");
                for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열을 반복
                    if (sock != d.Client_sock) { //보낸 클라이언트를 제외하는 부분
                    //    d.toClient_Obj.writeObject(new login_users(INDEX_, connect_, (ArrayList) ChatServer.Particiants.clone()));
                    //    d.toClient_Obj.flush();
                    } else {
                        temp_DATA = d;
                    }
                }
                if (!connect_)
                    if (!Connected_Clients.remove(temp_DATA))
                        System.out.println(temp_DATA + " : CC remove fail");
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

            while (true) { //수신을 기다리는 부분
                Object temp_Object = fromClient_Obj.readObject(); //Socket로부터 받은 데이터를 Object로 수신합니다.
                if (temp_Object instanceof user_SIGN) { //전달받은 객체가 사용자 타입일때
                    get_user_(((user_SIGN) temp_Object));
                } else if (temp_Object instanceof chat_) { //전달받은 객체가 채팅타입일 때
                    get_chat_((chat_) temp_Object);
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(sock + ": 연결 끊김 (" + ex + ")");
        } catch (Exception ex) {
            System.out.println(sock + ": 연결 끊김 원인 알 수 없음 (" + ex + ")");
        } finally {
            try {
                if (Client_DATA.login_NOW) {
                    ECHO_CONNECT(this.USER_DATA, false);
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

//        Oracle_jdbc dd = new Oracle_jdbc();
//        System.out.println(dd.check("admin", "PW"));
//        clear_DB();

        while (true) { //클라이언트의 접속을 대기하는 부분
            if (Connected_Clients.size() < 10) {
                ChatServer myServer = new ChatServer(serverSock.accept());
                myServer.start();
            }
        }
    }

    // 데이터베이스 청소
//    private static void clear_DB() throws SQLException {
//        MySql_jdbc dele = new MySql_jdbc();
//        dele.Delete_ALL_chat();
//        dele.Delete_ALL_user();
//    }

    // 클라이언트로 부터 받은 객체가 user_
    private void get_user_(user_SIGN temp_USER) throws Exception {
        System.out.println(temp_USER.ID_ + "----");
        if (temp_USER.login_ == 0) { //클라이언트의 로그인 시도
            Client_DATA.INDEX_ = get_User_JDBC(temp_USER.ID_, temp_USER.PW_.getBytes());
            if (Client_DATA.INDEX_ != 0) {
                synchronized (ChatServer.Connected_Clients) {
                    if (ChatServer.Connected_Clients.size() >= 10) {
                        Client_DATA.toClient_Obj.writeObject(new command(1, false, "서버의 정원이 가득 찼습니다."));
                        Client_DATA.toClient_Obj.flush();
                        System.out.println(sock + " : 수용인원 초과로 로그인 실패");
                        return;
                    }
                    this.USER_DATA = new user_DATA(Client_DATA.INDEX_, temp_USER.ID_, temp_USER.ID_);
                    Client_DATA.login_NOW = true;
                    ChatServer.Connected_Clients.add(Client_DATA);
                    Client_DATA.toClient_Obj.writeObject(new command(1, true, USER_DATA.ID_));
                    Client_DATA.toClient_Obj.flush();
                    Client_DATA.toClient_Obj.writeObject(new user_DATA(this.USER_DATA.INDEX_, this.USER_DATA.ID_, this.USER_DATA.NN_));
                    Client_DATA.toClient_Obj.flush();

                }
                synchronized (ChatServer.Particiants) {
                    ChatServer.Particiants.add(this.USER_DATA);
                    //Client_DATA.toClient_Obj.writeObject(new login_users(temp_USER.ID_, true, ChatServer.Particiants));
                    Client_DATA.toClient_Obj.flush();
                }

                ECHO_CONNECT(this.USER_DATA, true);

                System.out.println(sock + " : 로그인 성공");
            } else {
                Client_DATA.toClient_Obj.writeObject(new command(1, false, "ID 또는 PW를 확인해 주세요."));
                System.out.println(sock + " : 로그인 실패");
            }
        } else if (temp_USER.login_ == 2) { //클라이언트의 등록 시도
            System.out.println(temp_USER.ID_ + "--------------");
            String temp_string = set_User_JDBC(temp_USER.ID_, temp_USER.PW_.getBytes());
            if (temp_string == "") {
                Client_DATA.toClient_Obj.writeObject(new command(2, true, "등록 성공"));
                System.out.println(sock + " : 등록 성공");
            } else {
                Client_DATA.toClient_Obj.writeObject(new command(2, false, temp_string));
                System.out.println(sock + " : " + temp_string);
            }
        }
    }

    // 클라이언트로 부터 받은 객체가 chat_
    private void get_chat_(chat_ temp_CHAT) throws IOException, SQLException {
        System.out.print(sock);
        System.out.println(temp_CHAT);

        if (temp_CHAT.ISIT_group) {
            synchronized (Connected_Clients) {
                db.Insert_chat(temp_CHAT.SENDER_INDEX, temp_CHAT.RECEIVER_INDEX, true, temp_CHAT.chat_TEXT_);
                for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열을 반복
                    if (sock != d.Client_sock) { //보낸 클라이언트를 제외하는 부분
                        d.toClient_Obj.writeObject(temp_CHAT);
                        d.toClient_Obj.flush();
                    }
                }
            }
        } else {
            synchronized (Connected_Clients) {
                if (temp_CHAT.RECEIVER_INDEX == 0){
                    for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열 반복
                        if (sock != d.Client_sock) { //보낸 클라이언트를 제외하고 귓속말 상대를 찾는 부분
                            d.toClient_Obj.writeObject(temp_CHAT);
                            d.toClient_Obj.flush();
                        }
                    }
                } else {
                    db.Insert_chat(temp_CHAT.SENDER_INDEX, temp_CHAT.RECEIVER_INDEX, false, temp_CHAT.chat_TEXT_);
                    for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열 반복
                        if (sock != d.Client_sock && temp_CHAT.RECEIVER_INDEX == d.INDEX_) { //보낸 클라이언트를 제외하고 귓속말 상대를 찾는 부분
                            d.toClient_Obj.writeObject(temp_CHAT);
                            d.toClient_Obj.flush();
                            break;
                        }
                    }
                }
            }
        }
    }

    // 새로운 계정 만들기
    private String set_User_JDBC(String ID_, byte[] Password) throws Exception {
        String SALT = make_SALT();
        return db.Insert_user(ID_, Hashing(Password, SALT), SALT);
    }

    // 유저 정보와 대조한 뒤 로그인 하기
    private int get_User_JDBC(String ID_, byte[] password_) throws Exception {
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
    private String make_SALT() throws Exception {
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
