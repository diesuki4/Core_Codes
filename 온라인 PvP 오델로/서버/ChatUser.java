import java.io.*;
import java.net.*;

// 한 유저의 채팅 이용을 위한 정보들을 담은 클래스
public class ChatUser
{
    private String myId;    // 나의 아이디
    private String vsId;    // 상대의 아이디
    // 연결 정보
    private Socket socket;
    private BufferedReader br;  // 입력 스트림
    private PrintWriter pw;     // 출력 스트림
    
    public ChatUser()
    {
        this("", "", null, null , null);
    }
    
    public ChatUser(String myId, String vsId, Socket socket, BufferedReader br, PrintWriter pw)
    {
        setMyId(myId);
        setVsId(vsId);
        setBr(br);
        setPw(pw);
        setSocket(socket);
    }

    // getter/setter
    public String getMyId()            { return myId; }
    public String getVsId()            { return vsId; }
    public Socket getSocket()        { return socket; }
    public BufferedReader getBr()    { return br; }
    public PrintWriter getPw()        { return pw; }
    public void setMyId(String myId)            { this.myId = myId; }
    public void setVsId(String vsId)            { this.vsId = vsId; }
    public void setSocket(Socket socket)    { this.socket = socket; }
    public void setBr(BufferedReader br)    { this.br = br; }
    public void setPw(PrintWriter pw)        { this.pw = pw; }
}   // ChatUser Class
