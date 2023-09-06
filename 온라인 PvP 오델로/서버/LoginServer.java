import java.io.*;
import java.net.*;
import java.util.*;

/*
    현재 사용중인 계정 정보를 inUseVector에 저장하며,
    다른 서버들이 계정의 유효성을 검사할 때, 이 서버의
    isExist(), isValid() 등의 함수를 사용하게 된다.
*/

// 로그인 서버
// 아이디, 패스워드를 받아
// 해당 정보를 갖는 계정이 있는지, 현재 사용중인지 확인한 후
// 작업을 처리하여 결과를 전송한다.
// (로그인 성공시 유효성 검사용 100,000 이하의 랜덤 정수 문자열을 함께 전송한다.) 
public class LoginServer extends ServerSocket implements Runnable
{
    private Thread thread;
    
    // 현재 사용중인 계정 정보들을 벡터에 저장하여 관리한다.
    private Vector<InUseInfo> inUseVector;
    
    public LoginServer(int port) throws Exception
    {
        super(port);
        thread = new Thread(this);

        inUseVector = new Vector<InUseInfo>();

        // 싱클톤 패턴의 서버 매니저에 추가
        ServerManager.getInstance().setLoginServer(this);
    }

    // 로그인 서버의 현재 이용중인 계정 벡터를 반환
    public Vector<InUseInfo> getInUseVector()    { return inUseVector; }
    
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
    
    // 현재 계정이 이용중인지 확인하는 메서드
    public synchronized boolean isExist(String id)
    {
        int i;
        int size;

        size = inUseVector.size();
        
        for (i=0; i<size; i++)
            if (id.equals(inUseVector.get(i).getId()))
                return true;
        
        return false;    
    }
    
    // 전송된 아이디와 정수키가 유효한지 확인하는 메서드
    public synchronized boolean isValid(String id, String key)
    {
        int i;
        int size;

        size = inUseVector.size();
        
        for (i=0; i<size; i++)
            if (id.equals(inUseVector.get(i).getId()) && key.equals(inUseVector.get(i).getKey()))
                return true;
        
        return false;    
    }
    
    // 이용중인 계정 벡터에서 해당 정보를 갖는 객체의 인덱스를 검색하는 메서드
    public synchronized int find(String id)
    {
        int size = inUseVector.size();

        for (int i=0; i<size; i++)
            if (id.equals(inUseVector.get(i).getId()))
                return i;
        
        return -1;            
    }
    public synchronized int find(String id, String key)
    {
        int size = inUseVector.size();

        for (int i=0; i<size; i++)
            if (id.equals(inUseVector.get(i).getId()) && key.equals(inUseVector.get(i).getKey()))
                return i;
        
        return -1;            
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
                // 아이디, 패스워드
                String id = Encryptor.decrypt(br.readLine());
                String password = Encryptor.decrypt(br.readLine());
                
                // MemberDAO의 login은 DB에 해당 아이디와 패스워드를 갖는 계정이 있는지 검색하는 메서드이다.
                if (ServerManager.getInstance().getMemberDAO().login(id, password))
                {
                    // 해당 정보를 갖는 계정이 존재한다.
                    if (isExist(id))
                    {
                        // 이 계정은 이미 사용중이다.
                        pw.println(ServerConstants.LOGIN_INUSE);
                        
                        // 연결 종료
                        pw.flush();
                        pw.close();    
                        br.close();    
                        clnt.close();        
                    }
                    else
                    // 로그인 성공
                    {
                        // 유효성 검사용 100,000 이하의 랜덤 정수 문자열 생성
                        String key = Integer.toString((int)(Math.random()*100000 + 1));
                        
                        // 아이디와 키만 저장하면 되지만, 급히 제작하느라
                        // 또 다른 별도의 클래스를 만들지는 않았습니다.
                        inUseVector.add(new InUseInfo(id, key, null, null, null));
                        
                        // 로그인 성공 전송
                        pw.println(ServerConstants.LOGIN_SUCCESS);
                        // 정수키도 함께 전송
                        pw.println(Encryptor.encrypt(key));
                        pw.flush();
    
                        // 사용자의 로그아웃을 확인하는 쓰레드
                        (new Thread() {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    // 이 입력 대기 상태는 특별한 데이터를 받지 않고,
                                    // 클라이언트가 연결을 종료했을때의 예외 처리를
                                    // 이용해 로그아웃을 판별한다.
                                    String msg = br.readLine();
                                    if (msg == null) throw new Exception("Connection reset");
                                }    // 사용자의 로그 아웃을 확인하는 쓰레드의 try
                                catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                    
                                    // 해당 아이디와 키를 갖는 정보를 벡터에서 삭제
                                    int idx = find(id, key);
                                    inUseVector.remove(idx);

                                    // 매칭이 되기 전 뒤로가기 키를 누른 경우,
                                    // 클라이언트가 Poll Server(매칭 서버의 대기열에서 삭제하는 서버)에
                                    // 처리를 요청하지만,
                                    // 프로그램을 종료시켜 버린 경우, 처리되지 않고 남기 때문에,
                                    // 이 경우, 로그아웃의 판별과 함께 이 서버가 직접 Poll Server에 요청하게 된다.
                                    try
                                    {
                                        Socket s = new Socket(ServerConstants.SERVER_DOMAIN, ServerConstants.POLL_SERV_PORT);
                                        BufferedReader b = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF8"));
                                        PrintWriter p = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF8"));
    
                                        p.println(Encryptor.encrypt(id));
                                        p.println(Encryptor.encrypt(key));
                                        p.flush();
                                        
                                        // 매칭 서버의 대기열에 존재하면 삭제하고,
                                        // 존재하지 않으면 삭제하지 않는다.
                                        System.out.println(b.readLine());
    
                                        b.close();
                                        p.close();
                                        s.close();
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }    // 사용자의 로그 아웃을 확인하는 쓰레드의 catch
                                finally
                                {
                                    try
                                    {
                                        // 연결 종료
                                        br.close();
                                        pw.close();
                                        clnt.close();
                                    }
                                    catch (Exception excp)
                                    {
                                        excp.printStackTrace();
                                    }
                                    
                                    interrupt();    // 사용자의 로그 아웃을 확인하는 쓰레드 종료
                                }    // 사용자의 로그 아웃을 확인하는 쓰레드의 finally
                            }    // 사용자의 로그 아웃을 확인하는 쓰레드의 run()
                        }).start();    // 사용자의 로그 아웃을 확인하는 쓰레드
                    }    // if (isExist(id)) 의 else
                }    // if (ServerManager.getInstance().getMemberDAO().login(id, password))
                else
                {    // 해당 정보를 갖는 계정이 없다.
                    pw.println(ServerConstants.LOGIN_FAIL);
                    
                    // 연결 종료
                    pw.flush();
                    pw.close();    
                    br.close();    
                    clnt.close();
                }

                interrupt();    // 쓰레드 종료
            }    // ChildThread 의 run() 의 try
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }    // ChildThread 의 run()
    }    // ChildThread Inner Class
}    // LoginServer Class
