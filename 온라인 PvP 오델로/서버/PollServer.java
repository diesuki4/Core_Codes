import java.io.*;
import java.net.*;

/*
    클라이언트가 매칭을 요청하고
    매칭이 완료되기 전 뒤로 가기 키를 누른 경우,
    매칭 서버의 대기열에서 빠지지 않고 남게 된다.
    
    이를 해결하기 위해, 매칭이 완료되기 전
    뒤로 가기 키를 누르면, 이 서버에 작업을 요청하게 된다.
    
    프로그램을 닫은 경우는 이 서버에 요청이 들어오지 않기 때문에,
    로그인 서버가 로그 아웃의 판별과 함께 직접 이 서버에 요청하게 된다.
*/

// 매칭 서버의 대기열에서 삭제하는 서버
// 아이디, 정수키를 받아 작업을 처리하고 결과를 전송한다.
// (정수키를 확인한다.)
public class PollServer extends ServerSocket implements Runnable
{
    private Thread thread;
    
    public PollServer(int port) throws Exception
    {
        super(port);
        thread = new Thread(this);

        // 싱클톤 패턴의 서버 매니저에 추가
        ServerManager.getInstance().setPollServer(this);
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
    
    private class ChildThread extends Thread
    {
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
                // 아이디, 정수키
                String id = Encryptor.decrypt(br.readLine());
                String key = Encryptor.decrypt(br.readLine());
                
                // 해당 아이디, 정수키를 갖는 객체를 대기열에서 검색하여 삭제
                MatchServer matchServer = ServerManager.getInstance().getMatchServer();
                InUseInfo info = matchServer.findQueue(id, key);
                
                if (info != null)
                {
                    info.getBr().close();
                    info.getPw().close();
                    info.getSocket().close();
                    matchServer.getQueue().remove(info);
                    
                    // 매칭 서버의 대기열에서 삭제 성공
                    pw.println(ServerConstants.POLL_SUCCESS);
                }
                else
                {
                    // 그러한 정보를 갖는 객체가 대기열에 존재하지 않음
                    pw.println(ServerConstants.POLL_FAIL);
                }

                pw.flush();
                pw.close();    
                br.close();    
                clnt.close();
                
                interrupt();    // 쓰레드 종료
            }    // ChildThread 의 run() 의 try
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }    // ChildThread 의 run()
    }    // ChildThread Inner Class
}    // PollServer Class
