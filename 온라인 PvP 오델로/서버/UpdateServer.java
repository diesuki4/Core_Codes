import java.io.*;
import java.net.*;

// 전적 갱신 서버
// 게임의 승자가 전송한(둘다 탈주한 경우는 매칭 서버가 전송한)
// 승자 아이디, 승자 정수키, 패자 아이디, 게임 결과를 받아 작업을 처리하고 결과를 전송한다.
// (승자의 정수키를 확인한다.)
// (둘다 탈주하여 매칭 서버가 요청한 경우는 ServerConstants.UPDATE_MATCH_SERVER_REQUEST 라는 키가 전송된다.)
public class UpdateServer extends ServerSocket implements Runnable
{
    private Thread thread;
    
    public UpdateServer(int port) throws Exception
    {
        super(port);
        thread = new Thread(this);

        // 싱클톤 패턴의 서버 매니저에 추가
        ServerManager.getInstance().setUpdateServer(this);
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
                // 승자 아이디, 승자 정수키, 패자 아이디, 게임 결과
                String id = Encryptor.decrypt(br.readLine());
                String key = Encryptor.decrypt(br.readLine());
                String loser = Encryptor.decrypt(br.readLine());
                String updateConstant = br.readLine();
                
                // 승자와 패자의 전적 갱신을 위한 객체
                MemberDTO memberDTO = ServerManager.getInstance().getMemberDAO().info(id);
                MemberDTO memberDTOLoser = ServerManager.getInstance().getMemberDAO().info(loser);
                String result = "";
                
                if (memberDTO != null && memberDTOLoser != null)
                {
                    if (ServerManager.getInstance().getLoginServer().isValid(id, key) ||
                            key.equals(ServerConstants.UPDATE_MATCH_SERVER_REQUEST))
                    {    // 유효한 요청이거나 매칭 서버가 요청한 경우 두 유저의 전적 갱신
                        memberDTO.setTotal(memberDTO.getTotal() + 1);
                        memberDTOLoser.setTotal(memberDTOLoser.getTotal() + 1);

                        // 비겼을 경우 총 플레이 횟수만 증가하게 된다.
                        if (!updateConstant.equals(ServerConstants.MATCH_DRAW))
                        {
                            memberDTO.setWin(memberDTO.getWin() + 1);
                            
                            switch (updateConstant)
                            {
                            case ServerConstants.MATCH_WIN : memberDTOLoser.setLose(memberDTOLoser.getLose() + 1); break;
                            case ServerConstants.MATCH_ESCAPE : memberDTOLoser.setEscape(memberDTOLoser.getEscape() + 1); break;
                            }
                        }
                        
                        memberDTO.setWrate((double)memberDTO.getWin() / (double)memberDTO.getTotal());
                        memberDTOLoser.setWrate((double)memberDTOLoser.getWin() / (double)memberDTOLoser.getTotal());
                        
                        // DB 에 전적 갱신
                        ServerManager.getInstance().getMemberDAO().update(memberDTO);
                        ServerManager.getInstance().getMemberDAO().update(memberDTOLoser);
                        
                        // 전적 갱신 성공
                        result = ServerConstants.UPDATE_SUCCESS;
                    }
                    else
                    {    // 유효하지 않은 요청일 경우
                        result = ServerConstants.UPDATE_INVALID;
                    }
                }    // if (memberDTO != null && memberDTOLoser != null)
                else
                {    // 전적 갱신 실패
                    result = ServerConstants.UPDATE_FAIL;
                }
                
                pw.println(result);
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
}    // UpdateServer Class
