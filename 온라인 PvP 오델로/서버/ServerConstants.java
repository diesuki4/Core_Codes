/*
    이 프로그램의 서버는 비정상적인 이용에 대비하여
    제작되었기 때문에,  MATCH_INVALID, UPDATE_INVALID 등은
    버그가 아닌 이상, 클라이언트 프로그램을 통한 이용시는 발생하지 않는다.
*/

// 이 프로젝트의 클라이언트<->서버 간 일종의 프로토콜을 정의한 클래스
public class ServerConstants
{
    public static final String SERVER_DOMAIN = "www.example.com";
    // 이용할 DB 서버
    public static String DATABASE_IP =   "my.db.com";
    public static String DATABASE_PORT = "3306";
    public static String DATABASE_ID =   "privateid";
    public static String DATABASE_PW =   "privatepw";
/*    public static String DATABASE_IP = "localhost";    // 이 컴퓨터에서 실행중인 DB 서버를 이용할 경우
    public static String DATABASE_PORT = "3306";
    public static String DATABASE_ID = "ID";
    public static String DATABASE_PW = "PASSWORD";
*/

    public static final int REGISTER_SERV_PORT = 32433;    // 회원 가입 서버 포트
    public static final int WITHDRAWAL_SERV_PORT = 32434;    // 회원 탈퇴 서버 포트
    public static final int CHANGE_SERV_PORT = 32435;    // 비밀번호 변경 서버 포트
    public static final int SEARCH_SERV_PORT = 32436;    // 전적 검색 서버 포트
    public static final int LOGIN_SERV_PORT = 32437;    // 로그인 서버 포트
    public static final int MATCH_SERV_PORT = 32438;    // 매칭 서버 포트
    public static final int UPDATE_SERV_PORT = 32439;    // 전적 갱신 서버 포트
    public static final int POLL_SERV_PORT = 32440;    // 매칭 서버의 대기열에서 삭제를 요청하는 서버 포트
    public static final int CHAT_SERV_PORT = 32441;    // 채팅 서버 포트
    
    public static final String REGISTER_SUCCESS = "RS";    // 회원 가입 성공
    public static final String REGISTER_EXIST = "RE";    // 중복된 아이디 존재

    public static final String WITHDRAWAL_SUCCESS = "WS";    // 회원 탈퇴 성공
    public static final String WITHDRAWAL_FAIL = "WF";    // 회원 탈퇴 실패
    
    public static final String CHANGE_SUCCESS = "CS";    // 비밀번호 변경 성공
    public static final String CHANGE_FAIL = "CF";    // 비밀번호 변경 실패
    
    public static final String SEARCH_SUCCESS = "SS";    // 전적 검색 성공
    public static final String SEARCH_FAIL = "SF";    // 전적 검색 실패

    public static final String LOGIN_INUSE = "LI";    // 해당 계정이 이미 사용중
    public static final String LOGIN_SUCCESS = "LS";    // 로그인 성공
    public static final String LOGIN_FAIL = "LF";    // 로그인 실패

    // 매칭이 된후 쓰레드를 생성하여 게임이 시작되면,
    // 게임 진행 쓰레드가 입력을 한쪽씩 받아 처리하기 때문에
    // 내 턴에 상대가 나가면, 내가 돌을 두고 차례가 바뀌어야 상대의 탈주 사실을 고지 받는다.
    
    // 만약 내턴에 상대가 나가고 차례가 바뀌지 않은채로 내가 나갔을 경우, 서버의 내 연결에서 예외가 발생한다.
    // 이 때, 상대에게 탈주 사실을 알리고 MATCH_ALIVE 가 되돌아 오면 아직 상대가 나가지 않았다는 것을 판별하기 위해 사용된다.
    public static final String MATCH_ALIVE = "MA";    // MATCH_ALIVE
    public static final String MATCH_VALID = "MVLD";    // 계정이 유효하여 대기열에 등록됨
    public static final String MATCH_INVALID = "MIVLD";    // 계정이 유효하지 않음
    
    public static final String MATCH_BLACK = "B";    // 나의 돌색은 검정색
    public static final String MATCH_WHITE = "W";    // 나의 돌색은 하얀색
    /* 바둑판의 상태를 인코딩 한 8x8 총 64 길이의 문자열을 구성하는 상태들 */
    // char 로 정의한 이유는 String.charAt(idx) 과의 비교시 편의성을 얻기 위함
    public static final char MATCH_CHAR_BLACK = 'B';
    public static final char MATCH_CHAR_WHITE = 'W';
    public static final char MATCH_CHAR_EMPTY = 'E';
    /* 바둑판의 상태를 인코딩 한 8x8 총 64 길이의 문자열을 구성하는 상태들 */
    
    public static final String MATCH_OK = "MO";    // 해당 위치에 돌을 둘 수 있어 처리했다
    public static final String MATCH_YOUR_TURN = "MYT";    // 너의 차례이다
    public static final String MATCH_CANT_PUT = "MCNTP";    // 그곳에 돌을 둘 수 없다
    public static final String MATCH_PASS = "MP";    // 너가 패스해야 한다
    public static final String MATCH_VS_PASS = "MVP";    // 상대가 패스해야 한다
    
    public static final String MATCH_WIN = "MWIN";    // 승리
    public static final String MATCH_LOSE = "MLOSE";    // 패배
    public static final String MATCH_ESCAPE = "MESC";    // 상대가 탈주
    public static final String MATCH_DRAW = "MDRW";    // 비겼다
    
    public static final String UPDATE_INVALID = "UI";    // 계정이 유효하지 않음
    public static final String UPDATE_SUCCESS = "US";    // 전적 갱신 성공
    public static final String UPDATE_FAIL = "UF";    // 전적 갱신 실패
    // 전적 갱신 서버에서 정수키로 이 문자열이 전송되면,
    // 두 유저가 모두 전적 갱신 서버에 요청을 하지 않고 나가,
    // 매칭 서버가 직접 전적 갱신 서버에 두 유저의 전적 갱신을 요청한 경우이다.
    public static final String UPDATE_MATCH_SERVER_REQUEST = "UMSR";    // UPDATE_MATCH_SERVER_REQUEST
    
    public static final String POLL_SUCCESS = "PS";    // 매칭 서버의 대기열에서 삭제 완료
    public static final String POLL_FAIL = "PF";    // 그러한 정보를 갖는 객체가 대기열에 존재하지 않음
}
