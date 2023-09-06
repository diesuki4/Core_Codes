// 싱글톤 패턴의 서버 매니저
public class ServerManager
{
    private static final ServerManager instance = new ServerManager();
    
    private RegisterServer registerServer;
    private WithdrawalServer withdrawalServer;
    private ChangeServer changeServer;
    private SearchServer searchServer;
    private LoginServer loginServer;
    private MatchServer matchServer;
    private UpdateServer updateServer;
    private PollServer pollServer;
    private ChatServer chatServer;
    private MemberDAO memberDAO;

    // getter
    public static ServerManager getInstance()        { return instance; }
    
    public RegisterServer getRegisterServer()        { return registerServer; }
    public WithdrawalServer getWithdrawalServer()    { return withdrawalServer; }
    public ChangeServer getChangeServer()            { return changeServer; }
    public SearchServer getSearchServer()            { return searchServer; }
    public LoginServer getLoginServer()                { return loginServer; }
    public MatchServer getMatchServer()                { return matchServer; }
    public UpdateServer getUpdateServer()            { return updateServer; }
    public PollServer getPollServer()                { return pollServer; }
    public ChatServer getChatServer()                { return chatServer; }
    public MemberDAO getMemberDAO()                    { return memberDAO; }
    
    // setter
    public void setRegisterServer(RegisterServer registerServer)        { this.registerServer = registerServer; }
    public void setWithdrawalServer(WithdrawalServer withdrawalServer)    { this.withdrawalServer = withdrawalServer; }
    public void setChangeServer(ChangeServer changeServer)                { this.changeServer = changeServer; }
    public void setSearchServer(SearchServer searchServer)                { this.searchServer = searchServer; }
    public void setLoginServer(LoginServer loginServer)                    { this.loginServer = loginServer; }
    public void setMatchServer(MatchServer matchServer)                    { this.matchServer = matchServer; }
    public void setUpdateServer(UpdateServer updateServer)                { this.updateServer = updateServer; }
    public void setPollServer(PollServer pollServer)                    { this.pollServer = pollServer; }
    public void setChatServer(ChatServer chatServer)                    { this.chatServer = chatServer; }
    public void setMemberDAO(MemberDAO memberDAO)                        { this.memberDAO = memberDAO; }
}