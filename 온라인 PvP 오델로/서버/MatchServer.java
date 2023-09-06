import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// 매칭 서버
// 아이디, 정수키를 받아 유효한 계정인지 확인후 대기열에 삽입하고 결과를 전송한다.
// 대기열의 크기가 2 이상이면 2 개의 객체를 꺼내어 매칭시키고
// 쓰레드를 생성하여 게임을 진행한다.
// (정수키를 확인한다.)
public class MatchServer extends ServerSocket implements Runnable
{
    private Thread thread;
    
    // 매칭을 대기중인 유저들을 저장하는 대기열
    private LinkedBlockingQueue<InUseInfo> queue;
    
    public MatchServer(int port) throws Exception
    {
        super(port);
        thread = new Thread(this);
        
        queue = new LinkedBlockingQueue<InUseInfo>();

        // 싱클톤 패턴의 서버 매니저에 추가
        ServerManager.getInstance().setMatchServer(this);
    }

    // 매칭 서버의 대기열을 반환
    public LinkedBlockingQueue<InUseInfo> getQueue()    { return queue; }

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
    
    // 대기열에서 해당 정보를 갖는 객체를 반환하는 메서드
    // Poll Server 에서 사용된다.
    public synchronized InUseInfo findQueue(String id, String key)
    {
        for (InUseInfo info : queue)
            if (info.getId().equals(id) && info.getKey().equals(key))
                return info;
        return null;
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
                String result;
                
                System.out.println(id);
                System.out.println(key);
                
                if (ServerManager.getInstance().getLoginServer().isValid(id, key))
                    // 유효한 계정
                    result = ServerConstants.MATCH_VALID;
                else
                    // 유효하지 않은 계정
                    result = ServerConstants.MATCH_INVALID;

                System.out.println(result);
                pw.println(result);
                pw.flush();

                if (result.equals(ServerConstants.MATCH_VALID))
                {    // 유효한 계정일 경우
                    
                    // 대기열에 방금 사용한 연결 정보와 함께 추가
                    queue.offer(new InUseInfo(id, key, clnt, br, pw));

                    // 대기열의 크기가 2 이상이면 2 개의 객체를 꺼내어 매칭시키고
                    // 쓰레드를 생성하여 게임 진행
                    if (2 <= queue.size())
                        (new IOControlThread(queue.poll(), queue.poll())).start();
                }
                else
                {    // 유효하지 않은 계정일 경
                    br.close();
                    pw.close();
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
    
    // 매칭된 두 유저의 게임을 진행하는 클래스
    private class IOControlThread extends Thread
    {
        // 오델로를 실행하는 코어
        private Reversi reversi;
        
        // 흑돌과 백돌의 아이디
        private String idBlack, idWhite;
        // 정수키는 사용되지 않는다.

        // 흑돌과 백돌의 연결 정보
        private Socket sockBlack, sockWhite;
        private BufferedReader blackRd, whiteRd;
        private PrintWriter blackPw, whitePw;
        
        public IOControlThread(InUseInfo infoBlack, InUseInfo infoWhite)
        {
            reversi = new Reversi();
            
            idBlack = infoBlack.getId();
            idWhite = infoWhite.getId();
            
            sockBlack = infoBlack.getSocket();
            sockWhite = infoWhite.getSocket();
            blackRd = infoBlack.getBr();
            whiteRd = infoWhite.getBr();
            blackPw = infoBlack.getPw();
            whitePw = infoWhite.getPw();
        }
        
        @Override
        public void run()
        {
            // 예외가 발생한 시점에서 흑백 중 누구에게서 발생한 것인지 저장하기 위한 변수
            Status escape = Status.BLACK;
        
            // 각각의 돌색과 상대의 아이디 전송
            blackPw.println(Encryptor.encrypt(idWhite));
            blackPw.println(ServerConstants.MATCH_BLACK);
            whitePw.println(Encryptor.encrypt(idBlack));
            whitePw.println(ServerConstants.MATCH_WHITE);
            
            // 초기 바둑판의 상태 전송
            String encodedStatus = toEncodedStatus();
            blackPw.println(encodedStatus);
            whitePw.println(encodedStatus);            
            
            blackPw.flush();
            whitePw.flush();
            
            try
            {
                while (true)
                {    
                    // 현재 시점에서 예외가 발생할 경우 누구에게서 발생한 것인지 저장
                    escape = (reversi.getTurn() == Status.BLACK ? Status.BLACK : Status.WHITE);
                    
                    // 현재 차례의 유저에게서 위치를 전송 받는다.
                    String str = (reversi.getTurn() == Status.BLACK ? blackRd : whiteRd).readLine();
                    if (str == null) throw new Exception("Connection reset");
                    Position pos = strToPosition(str);
                    
                    // 입력 받은 위치를 이용해 코어가 처리한다.
                    if (reversi.handleInput(pos.getRow(), pos.getCol()))
                    {
                        // 게임이 종료되었을 경우
                        if (reversi.isEnd())
                        {
                            // 게임의 결과 전송 (승리, 패배, 비김)
                            (reversi.getBlackCount() > reversi.getWhiteCount() ? blackPw : whitePw)
                                    .println((reversi.getBlackCount() == reversi.getWhiteCount() ? ServerConstants.MATCH_DRAW : ServerConstants.MATCH_WIN));
                            (reversi.getBlackCount() > reversi.getWhiteCount() ? whitePw : blackPw)
                                    .println((reversi.getBlackCount() == reversi.getWhiteCount() ? ServerConstants.MATCH_DRAW : ServerConstants.MATCH_LOSE));
                            // 현재 바둑판의 상태 전송
                            blackPw.println(toEncodedStatus());        
                            whitePw.println(toEncodedStatus());
                            blackPw.flush();        
                            whitePw.flush();                
                            
                            break;
                        }
                        // 현재 차례의 유저가 패스인 경우
                        else if (reversi.isPass())
                        {
                            // 각각에게 당신이 패스이고, 상대가 패스라는 사실 전달
                            (reversi.getTurn() == Status.BLACK ? blackPw : whitePw).println(ServerConstants.MATCH_PASS);
                            (reversi.getTurn() == Status.BLACK ? whitePw : blackPw).println(ServerConstants.MATCH_VS_PASS);
                            // 현재 바둑판의 상태 전송
                            blackPw.println(toEncodedStatus());        
                            whitePw.println(toEncodedStatus());
                            blackPw.flush();        
                            whitePw.flush();
                            
                            // 차례 변경
                            reversi.toggleTurn();
                        }
                        // 일반적으로 게임 진행
                        else
                        {
                            // 한쪽으로 당신의 차례이고, 한쪽으로 입력한 위치가 정상적으로 처리되었다고 전달 
                            (reversi.getTurn() == Status.BLACK ? blackPw : whitePw).println(ServerConstants.MATCH_YOUR_TURN);
                            (reversi.getTurn() == Status.BLACK ? whitePw : blackPw).println(ServerConstants.MATCH_OK);
                            // 현재 바둑판의 상태 전송
                            blackPw.println(toEncodedStatus());        
                            whitePw.println(toEncodedStatus());
                            blackPw.flush();        
                            whitePw.flush();
                        }
                    }    // if (reversi.handleInput(pos.getRow(), pos.getCol()))
                    else
                    {
                        // 해당 위치에 돌을 둘 수 없는 경우,
                        // 방금 돌을 둔 유저에게만 돌을 둘 수 없음을 알림
                        (reversi.getTurn() == Status.BLACK ? blackPw : whitePw).println(ServerConstants.MATCH_CANT_PUT);
                        // 방금 돌을 둔 유저에게만 현재 바둑판의 상태 전송
                        (reversi.getTurn() == Status.BLACK ? blackPw : whitePw).println(toEncodedStatus());
                        (reversi.getTurn() == Status.BLACK ? blackPw : whitePw).flush();
                    }
                }
            }    // IOControlThread 의 run() 의 첫번째 try
            catch (Exception ex)
            {
                ex.printStackTrace();
                
                if (escape == Status.BLACK)
                {    // 흑돌에서 읽는 동안 예외가 발생한 경우
                    
                    // 백돌에게 흑돌의 탈주 사실을 알림
                    whitePw.println(ServerConstants.MATCH_ESCAPE);
                    // 현재 바둑판의 상태 전송
                    whitePw.println(toEncodedStatus());
                    whitePw.flush();
                    
                    try
                    {
                        /*
                            쓰레드가 입력을 한쪽씩 받아 처리하기 때문에,
                            내 턴에 상대가 나가면, 내가 돌을 두고 차례가 바뀌어야 상대의 탈주 사실을 고지 받는다.
                            
                            만약 내턴에 상대가 나가고 차례가 바뀌지 않은채로 내가 나갔을 경우, 서버의 내 연결에서 예외가 발생한다.
                            이 때, 상대가 먼저 탈주했는지 판별하기 위한 처리가 필요하다.
                        */
                        
                        // 백돌이 탈주 사실을 전달 받고 MATCH_ALIVE 를 전송한 경우,
                        // 백돌이 전적 갱신 서버에 처리를 요청하게 된다.
                        
                        // 백돌에게서 응답이 없는 경우,
                        // 백돌이 흑돌보다 먼저 탈주했음을 판별할 수 있다.
                        if (whiteRd.readLine() == null)
                            throw new Exception("Connection reset");
                    }    // 백돌이 먼저 탈주했는지 확인하기 위한 try
                    catch (Exception e)
                    {    // 내 턴에 상대가 나가고, 차례가 바뀌지 않은채로 나도 나간 경우
                        // 이 서버가 직접 전적 갱신 서버에 전적 갱신을 요청한다.
                        try
                        {
                            Socket s = new Socket(ServerConstants.SERVER_DOMAIN, ServerConstants.UPDATE_SERV_PORT);
                            BufferedReader b = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF8"));
                            PrintWriter p = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF8"));

                            // 승자, 정수키로 매칭 서버의 요청 전달, 패자, 게임의 결과
                            p.println(Encryptor.encrypt(idBlack));
                            p.println(Encryptor.encrypt(ServerConstants.UPDATE_MATCH_SERVER_REQUEST));
                            p.println(Encryptor.encrypt(idWhite));
                            p.println(ServerConstants.MATCH_ESCAPE);
                            p.flush();
                            
                            System.out.println(b.readLine());

                            b.close();
                            p.close();
                            s.close();
                        }
                        catch (Exception excp)
                        {
                            excp.printStackTrace();
                        }
                    }    // 백돌이 먼저 탈주했는지 확인하기 위한 catch
                }    // if (escape == Status.BLACK)
                else if (escape == Status.WHITE)
                {    // 백돌에서 읽는 동안 예외가 발생한 경우
                    
                    // 흑돌에게 백돌의 탈주 사실을 알림
                    blackPw.println(ServerConstants.MATCH_ESCAPE);
                    // 현재 바둑판의 상태 전송
                    blackPw.println(toEncodedStatus());
                    blackPw.flush();
                    
                    try
                    {
                        /*
                            쓰레드가 입력을 한쪽씩 받아 처리하기 때문에,
                            내 턴에 상대가 나가면, 내가 돌을 두고 차례가 바뀌어야 상대의 탈주 사실을 고지 받는다.
                            
                            만약 내턴에 상대가 나가고 차례가 바뀌지 않은채로 내가 나갔을 경우, 서버의 내 연결에서 예외가 발생한다.
                            이 때, 상대가 먼저 탈주했는지 판별하기 위한 처리가 필요하다.
                        */
                        
                        // 흑돌이 탈주 사실을 전달 받고 MATCH_ALIVE 를 전송한 경우,
                        // 흑돌이 전적 갱신 서버에 처리를 요청하게 된다.
                        
                        // 흑돌에게서 응답이 없는 경우,
                        // 흑돌이 백돌보다 먼저 탈주했음을 판별할 수 있다.
                        if (blackRd.readLine() == null)
                            throw new Exception("Connection reset");
                    }    // 흑돌이 먼저 탈주했는지 확인하기 위한 try
                    catch (Exception e)
                    {    // 내 턴에 상대가 나가고, 차례가 바뀌지 않은채로 나도 나간 경우
                        // 이 서버가 직접 전적 갱신 서버에 전적 갱신을 요청한다.
                        try
                        {
                            Socket s = new Socket(ServerConstants.SERVER_DOMAIN, ServerConstants.UPDATE_SERV_PORT);
                            BufferedReader b = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF8"));
                            PrintWriter p = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF8"));

                            // 승자, 정수키로 매칭 서버의 요청 전달, 패자, 게임의 결과
                            p.println(Encryptor.encrypt(idWhite));
                            p.println(Encryptor.encrypt(ServerConstants.UPDATE_MATCH_SERVER_REQUEST));
                            p.println(Encryptor.encrypt(idBlack));
                            p.println(ServerConstants.MATCH_ESCAPE);
                            p.flush();
                            
                            System.out.println(b.readLine());

                            b.close();
                            p.close();
                            s.close();
                        }
                        catch (Exception excp)
                        {
                            excp.printStackTrace();
                        }
                    }    // 흑돌이 먼저 탈주했는지 확인하기 위한 catch                    
                }    // else if (escape == Status.WHITE)
            }    // IOControlThread 의 run() 의 첫번째 catch
            
            try
            {
                blackRd.close();
                blackPw.close();
                sockBlack.close();
                whiteRd.close();
                whitePw.close();
                sockWhite.close();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            interrupt();    // 게임 진행 쓰레드 종료
        }    // IOControlThread 의 run()
        
        // 바둑판의 상태를 8x8 총 64 길이의 문자열로 변환하는 메서드 (EEEBBWBWWEEEE...)
        private synchronized String toEncodedStatus()
        {
            String encodedStatus = "";
            
            for (int i=1; i<=Reversi.BOARD_SIZE; i++)
                for (int j=1; j<=Reversi.BOARD_SIZE; j++)
                    if (reversi.statusAt(i, j) == Status.BLACK)
                        encodedStatus += Character.toString(ServerConstants.MATCH_CHAR_BLACK);
                    else if (reversi.statusAt(i, j) == Status.WHITE)
                        encodedStatus += Character.toString(ServerConstants.MATCH_CHAR_WHITE);
                    else if (reversi.statusAt(i, j) == Status.EMPTY)
                        encodedStatus += Character.toString(ServerConstants.MATCH_CHAR_EMPTY);
            
            return encodedStatus;
        }
        
        // 예시 "34" 로 된 문자열을 위치 (3, 4) 로 변환하여 반환하는 메서드
        private synchronized Position strToPosition(String str)
        {
            return new Position((int)(str.charAt(0) - '0'), (int)(str.charAt(1) - '0'));
        }
    }    // IOControlThread Inner Class
}    // MatchServer Class
