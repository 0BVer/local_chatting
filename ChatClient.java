package chat;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

class ServerHandler extends Thread{
    private boolean stop;
    Socket sock = null;
    public ServerHandler(Socket sock){
        this.sock = sock;
    }
    command temp_COMMAND;
    chat_ temp_CHAT;
    boolean firstRun = true;
    ObjectInputStream fromServer_OBJ;

    public void run(){
        InputStream fromServer = null;
        try{
            if (firstRun){
                firstRun = false;
                fromServer = sock.getInputStream();
                fromServer_OBJ = new ObjectInputStream(fromServer);
            }

            while (!stop){ //채팅 수신을 기다리는 부분 (스트림이 종료되면 -1이 됨)
                Object temp_Object = (Object) fromServer_OBJ.readObject(); //Socket로부터 받은 데이터를 Object로 수신합니다.
                if (temp_Object instanceof command){ //전달받은 객체가 사용자 타입일때
                    temp_COMMAND = (command) temp_Object;
                    System.out.println(temp_COMMAND.message);
                    if (!temp_COMMAND.state)
                        System.out.println("다시 시도해 주시길 바랍니다.");
//                    if (temp_COMMAND.command_type == 1) { //클라이언트의 로그인에 대한 응답
//                        System.out.println(temp_COMMAND.message);
//                    } else if (temp_COMMAND.command_type == 2) { //클라이언트의 등록에 대한 응답
//                        System.out.println(temp_COMMAND.message);
//                    }

                } else if (temp_Object instanceof chat_){ //전달받은 객체가 채팅타입일 때
                    temp_CHAT = (chat_) temp_Object;
                    System.out.println(temp_CHAT.toString());
                }
            }
        } catch (IOException | ClassNotFoundException ex){
            System.out.println("연결 종료 (" + ex + ")");
        } finally {
            try{
                if (fromServer != null && !stop)
                    fromServer.close();
                if (sock != null && !stop)
                    sock.close();
            } catch (IOException ex){
            }
        }

    }
}

public class ChatClient {
    public static void main(String[] args) throws Exception {
        Socket sock = null;
        Scanner s = new Scanner(System.in);
        boolean Login_now = false;
        String temp_message;
        InputStream fromServer = null;

        try {
            sock = new Socket("localhost", 8888);
            System.out.println(sock + ": 연결됨");

            ServerHandler chandler = new ServerHandler(sock);

            OutputStream toServer = sock.getOutputStream();

            ObjectOutputStream toServer_Obj = new ObjectOutputStream(toServer);
            ObjectInputStream fromServer_Obj_login= new ObjectInputStream(sock.getInputStream());
            user_ USER = null;

            while (true){
                if (!Login_now) {
                    System.out.println(fromServer_Obj_login.available());
                    USER = new user_();
                    System.out.println("로그인 : 1, 등록 : 2");

                    if (s.nextInt() == 2) USER.login_ = 2; //등록으로 선택시 유저 객체의 로그인 시도를 등록 시도로 변경
                    else USER.login_ = 0;

                    System.out.println("ID를 입력해 주세요");
                    USER.ID_ = s.next();
                    System.out.println("PW를 입력해 주세요");
//                    USER.PW_ = new byte[]{1};
                    USER.setPW_(s.next().getBytes());

                    toServer_Obj.writeObject(USER);
                    toServer_Obj.flush();

                    Object temp_Object = fromServer_Obj_login.readObject();
                    if (temp_Object instanceof command) { //전달받은 객체가 사용자 타입일때
                        command temp_COMMAND = (command) temp_Object;
                        System.out.println(temp_COMMAND.message);
                        if (!temp_COMMAND.state) {
                            System.out.println("다시 시도해 주시길 바랍니다.");
//                            if (temp_COMMAND.command_type == 1) { //클라이언트의 로그인에 대한 응답
//                                System.out.println(temp_COMMAND.message);
//                            } else if (temp_COMMAND.command_type == 2) { //클라이언트의 등록에 대한 응답
//                                System.out.println(temp_COMMAND.message);
//                            }
                        } else if (temp_COMMAND.command_type == 1 && temp_COMMAND.state){ //로그인에 성공했을때
                            Login_now = true;
                            USER.login_ = 1;
                            temp_COMMAND = null;
//                            fromServer_Obj_login.close();
                            System.out.println(fromServer_Obj_login.available());
                            if (chandler.isInterrupted()){
                                System.out.println(1111);
                                chandler.run();
                            } else
                                chandler.start(); //서버에서 보내오는 값을 받기 위한 쓰레드 실행
                            if (chandler.isInterrupted()){
                                System.out.println(2222);
                            }
                            System.out.println(USER.ID_ + "님 채팅방에 오신걸 환영합니다. 텍스트를 입력해 주세요");
                        }
                    }
                } else{
                temp_message = s.nextLine();
                if (temp_message.equals("/")){
                    chandler.interrupt();
                    Login_now = false;
                    System.out.println("정상적으로 로그아웃 되었습니다.");
                    continue;
                }

                toServer_Obj.writeObject(new chat_(USER.ID_, temp_message));
                toServer_Obj.flush();
                }
            }
        } catch (UnknownHostException ex) {
            System.out.println("호스트를 찾을 수 없습니다. (" + ex + ")");
        } catch (IOException ex) {
            System.out.println("연결 종료 (" + ex + ")");
        } finally {
            try {
                if (sock != null)
                    sock.close();
            } catch (IOException ex) {
            }
        }
    }
}

