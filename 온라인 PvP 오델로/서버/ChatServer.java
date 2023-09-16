import java.io.*;
import java.net.*;
import java.util.*;

/*
    채팅 서버에 연결후,
    클라이언트가 전송한 나의 아이디, 상대의 아이디를 저장하고
    채팅 내용이 전송되면 벡터에서 상대의 아이디를 검색하여
    해당 연결로 보내주는 방식으로 동작한다.
*/

// 채팅 서버
// (유효성 검사용 정수키를 확인하지 않는다.)
public class ChatServer extends ServerSocket implements Runnable
{
    private Thread thread;
    private Vector<ChatUser> chatUsers;    // 현재 접속한 모든 채팅 유저들을 벡터로 관리한다.
    
    public ChatServer(int port) throws Exception
    {
        super(port);
        thread = new Thread(this);
        
        chatUsers = new Vector<ChatUser>();

        // 싱클톤 패턴의 서버 매니저에 추가
        ServerManager.getInstance().setChatServer(this);
    }

    public void start()
    {
        thread.start();
    }
    
    @Override
    public void run()
    {
        while (true)
            try { (new ChildThread(accept())).start(); }
            catch (Exception ex) { ex.printStackTrace(); }
    }

    // 벡터에서 해당 아이디가 나의 아이디인 객체를 반환한다.
    private synchronized ChatUser find(String id)
    {
        for (ChatUser info : chatUsers)
            if (info.getMyId().equals(id))
                return info;
        return null;
    }
    
    private class ChildThread extends Thread
    {
        private String myId;    // 나의 아이디
        private String vsId;    // 상대의 아이디
        
        // accept 함수에서 반환된 소켓을 이용한 연결 정보
        private Socket clnt;
        private BufferedReader br;
        private PrintWriter pw;

        public ChildThread(Socket clnt)
        {
            this.clnt = clnt;
            
            try
            {
                br = new BufferedReader(new InputStreamReader(clnt.getInputStream(), "UTF8"));
                pw = new PrintWriter(new OutputStreamWriter(clnt.getOutputStream(), "UTF8"));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }    // ChildThread Inner Class constructor
        
        @Override
        public void run()
        {
            try
            {
                // 나의 아이디, 상대의 아이디
                myId = Encryptor.decrypt(br.readLine());
                vsId = Encryptor.decrypt(br.readLine());
                
                // 정상적으로 전송되었다는 것을 확인차 다시 보내준다.
                pw.println(Encryptor.encrypt(myId));
                pw.println(Encryptor.encrypt(vsId));
                pw.flush();
                
                // 벡터에 나의 아이디, 상대의 아이디, 연결 정보를 추가한다.
                chatUsers.add(new ChatUser(myId, vsId, clnt, br, pw));
                
                // 이후, 채팅 입력이 들어오면,
                // 벡터에서 상대의 아이디를 검색하여
                // 해당 연결로 보내준다.
                while (true)
                {
                    String msg = br.readLine();
                    
                    if (msg == null) {
                        // 클라이언트가 연결을 종료한 경우
                        throw new Exception("Connection reset");
                    }
                    else {
                        // 정상적인 채팅 입력인 경우
                        send(msg);    // 상대에게 메세지 전달
                        // DB에 채팅 내역 로깅
                        ServerManager.getInstance().getMemberDAO().chatLogging(myId, vsId, msg);
                    }
                }
            }    // ChildThread의 run()의 첫번째 try
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            try
            {
                // 벡터에서 내 정보 삭제 및 연결 종료
                chatUsers.remove(find(myId));
                br.close();
                pw.close();
                clnt.close();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            interrupt();    // 쓰레드 종료
        }    // ChildThread의 run()
        
        // 상대에게 메세지를 전달한다.
        private synchronized void send(String msg)
        {
            for (ChatUser info : chatUsers)
            {   
                // 나의 아이디가 상대 아이디인 객체를 검색하고
                // 그 객체의 연결로 메세지를 전송한다.
                if (info.getMyId().equals(vsId))
                {
                    info.getPw().println(msg);
                    info.getPw().flush();
                    break;
                }
            }   // for (ChatUser info : chatUsers)
        }   // send(String msg)
    }   // ChildThread Inner Class
}   // ChatServer Class
