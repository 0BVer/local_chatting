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
    String USER_ID;
    boolean login_NOW = false;

    Server_DATA(Socket sock) {
        this.Client_sock = sock;
    }
}

public class ChatServer extends Thread {
    private static Server_DATA Client_DATA;
    private final Socket sock;

    //    private static final ArrayList<Socket> clients = new ArrayList<>(10); //클라이언트 소켓을 담는 배열
    private static final ArrayList<String> Particiants = new ArrayList<>(10); //클라이언트 이름 담는 배열 (공개)
    private static final ArrayList<Server_DATA> Connected_Clients = new ArrayList<>(10); //클라이언트 소켓을 담는 배열 (비공개)

    private static final int SALT_SIZE = 16;
    private static jdbc db = new jdbc();

//    public ChatServer(Socket sock) {this.sock = sock;}

    public ChatServer(Server_DATA Client_DATA) {
        this.Client_DATA = Client_DATA;
        this.sock = Client_DATA.Client_sock;
    }

    public void remove(Socket socket) {
        for (Server_DATA d : ChatServer.Connected_Clients) {
            if (socket == d.Client_sock) {
                ChatServer.Connected_Clients.remove(Client_DATA);
                break;
            }
        }
    }

//    public void remove(Socket socket){
//        //클라이언트 배열에서 클라이언트 소켓 제거
//        for (Socket s : ChatServer.clients){
//            if (socket == s){
//                ChatServer.clients.remove(socket);
//                break;
//            }
//        }
//    }

    public void ECHO_CONNECT(String ID_, boolean connect_, ObjectOutputStream toClient_echo) throws IOException {
        for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열을 반복
            if (sock != d.Client_sock) { //보낸 클라이언트를 제외하는 부분
                ObjectOutputStream toOtherClient_Obj = new ObjectOutputStream(d.Client_sock.getOutputStream());
                toOtherClient_Obj.writeObject(new command(3, connect_, new String[]{ID_}));
                toClient_echo.flush();
            }
        }
    }

    public void run() {
        //쓰레드가 할 일
        InputStream fromClient = null;
        OutputStream toClient = null;
        ObjectInputStream fromClient_Obj;
        ObjectOutputStream toClient_Obj = null;

        user_ temp_USER = null;
        command temp_COMMAND = null;
        String temp_string = "";
        try {
            System.out.println(sock + ": 연결됨");

            fromClient_Obj = new ObjectInputStream(sock.getInputStream()); //InputStream의 최종 형식을 Object로 설정해줍니다.
            toClient_Obj = new ObjectOutputStream(sock.getOutputStream());

            while (true) { //채팅 수신을 기다리는 부분 (스트림이 종료되면 -1이 됨)
                Object temp_Object = fromClient_Obj.readObject(); //Socket로부터 받은 데이터를 Object로 수신합니다.
                if (temp_Object instanceof user_) { //전달받은 객체가 사용자 타입일때
                    temp_USER = (user_) temp_Object;

                    if (temp_USER.login_ == 0) { //클라이언트의 로그인 시도
                        if (get_User(temp_USER.ID_, temp_USER.PW_.getBytes())) {

                            ChatServer.Particiants.add(temp_USER.ID_);
                            String[] P_array = new String[Particiants.size()];
                            int size = 0;
                            for (String temp : Particiants)
                                if (temp != null) P_array[size++] = temp;

                            toClient_Obj.writeObject(new command(1, true, P_array));
                            Client_DATA.USER_ID = temp_USER.ID_;

                            ECHO_CONNECT(temp_USER.ID_, true, toClient_Obj);
                            System.out.println(sock + " : 로그인 성공");
                        } else {
                            toClient_Obj.writeObject(new command(1, false, new String[]{"ID 또는 PW를 확인해 주세요."}));
                            System.out.println(sock + " : 로그인 실패");
                        }
                    } else if (temp_USER.login_ == 2) { //클라이언트의 등록 시도
                        temp_string = set_User(temp_USER.ID_, temp_USER.PW_.getBytes());

                        if (temp_string == "") {
                            toClient_Obj.writeObject(new command(2, true, new String[]{"등록 성공"}));
                            System.out.println(sock + " : 등록 성공");
                        } else {
                            toClient_Obj.writeObject(new command(2, false, new String[]{temp_string}));
                            System.out.println(sock + " : " + temp_string);
                        }
                    }
                } else if (temp_Object instanceof chat_) { //전달받은 객체가 채팅타입일 때
                    chat_ temp_CHAT = (chat_) temp_Object;

                    db.Insert_chat(temp_CHAT.ID_, temp_CHAT.chat_TEXT_, temp_CHAT.upload_TIME_);

                    if (!temp_CHAT.SILENT_CHAT) {
                        for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열을 반복
                            if (sock != d.Client_sock) { //보낸 클라이언트를 제외하는 부분
                                ObjectOutputStream toOtherClient_Obj = new ObjectOutputStream(d.Client_sock.getOutputStream());
                                toOtherClient_Obj.writeObject(temp_CHAT);
                                toClient_Obj.flush();
                            }
                        }
                        toClient_Obj.writeObject(temp_CHAT);
                        System.out.println(temp_CHAT);
                    } else {
                        for (String u : temp_CHAT.SILENT) { //귓속말 상대 배열 반복
                            boolean isThere = false;
                            for (Server_DATA d : ChatServer.Connected_Clients) { //클라이언트 배열 반복
                                if (sock != d.Client_sock && u == d.USER_ID) { //보낸 클라이언트를 제외하고 귓속말 상대를 찾는 부분
                                    ObjectOutputStream toOtherClient_Obj = new ObjectOutputStream(d.Client_sock.getOutputStream());
                                    toOtherClient_Obj.writeObject(temp_CHAT);
                                    toClient_Obj.flush();
                                    isThere = true;
                                    break;
                                }
                            }
                            if (!isThere) temp_string += u + ", ";
                        }
                        if (temp_string != "") {
                            toClient_Obj.writeObject(new chat_("SERVER ALERT", temp_string.substring(0, temp_string.length() - 2) + "를 서버에서 찾을 수 없습니다.", "", true, new String[]{Client_DATA.USER_ID}));
                            temp_string = "";
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(sock + ": 에러 (" + ex + ")");
        } catch (Exception ex) {
            System.out.println(sock + ": 에러 (" + ex + ")");
        } finally {
            try {
                if (sock != null) { //클라이언트가 접속을 종료했을때 소켓을 지우는 부분
                    ECHO_CONNECT(temp_USER.ID_, false, toClient_Obj);
                    Particiants.remove(temp_USER.ID_);
                    remove(Client_DATA.Client_sock);
                    sock.close();
//                    remove(sock);
                }
                toClient = null;
            } catch (IOException ex) {
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        ServerSocket serverSock = new ServerSocket(8888);
        System.out.println(serverSock + ": 서버 소켓 생성");

//        jdbc dele = new jdbc(); //데이터베이스 청소
//        dele.Delete_ALL_user();
//        dele.Delete_ALL_chat();

        while (true) { //클라이언트의 접속을 대기하는 부분
//            Socket client = serverSock.accept();
//            clients.add(client);
//            ChatServer myServer = new ChatServer(client);

            Server_DATA CLIENT = new Server_DATA(serverSock.accept());

            ChatServer myServer = new ChatServer(CLIENT);
            Connected_Clients.add(CLIENT);
            myServer.start();
        }
    }

    void check_DB() throws SQLException {
        Table table = new Table("user");
        db.Select_All(table);
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

        if (db.check(ID_, temp_pass)) return true; // db 에 저장된 아이디와 비밀번호를 대조한
        else return false;
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
