package chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient_rep {
    public static void main(String[] args) {
        Socket sock = null;

        try {
            sock = new Socket("localhost", 8888);
            System.out.println(sock + ": 연결됨");

            OutputStream toServer = sock.getOutputStream();

            //서버에서 보내오는 값을 받기 위한 쓰레드
            ServerHandler chandler = new ServerHandler(sock);
            chandler.start();

            byte[] buf = new byte[1024];
            int count;

            while ((count = System.in.read(buf)) != -1){
                toServer.write(buf, 0, count);
                toServer.flush();
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
