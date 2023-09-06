public class Main
{
    public static void main(String[] args)
    {
        // 이용할 DB 서버를 임의로 설정할 수 있다
        if (4 < args.length || (args.length == 1 && args[0].equals("-help")))
        {
            System.out.println("USAGE 1 : java -jar *.jar");
            System.out.println("USAGE 2 : java -jar *.jar <DATABASE_IP> <DATABASE_PORT> <DATABASE_ID> <DATABASE_PW>");
            return;        
        }
        else
        {
            System.out.println("Execute with '-help' argument to see other usages.");        
            
            if (args.length == 4)
            {
                ServerConstants.DATABASE_IP = args[0];
                ServerConstants.DATABASE_PORT = args[1];
                ServerConstants.DATABASE_ID = args[2];
                ServerConstants.DATABASE_PW = args[3];
                System.out.println("[DATABASE SERVER] " + args[0] + ":" + args[1]);
            }
        }

        try
        {
            (new RegisterServer(ServerConstants.REGISTER_SERV_PORT)).start();
            (new WithdrawalServer(ServerConstants.WITHDRAWAL_SERV_PORT)).start();
            (new ChangeServer(ServerConstants.CHANGE_SERV_PORT)).start();
            (new SearchServer(ServerConstants.SEARCH_SERV_PORT)).start();
            (new LoginServer(ServerConstants.LOGIN_SERV_PORT)).start();
            (new MatchServer(ServerConstants.MATCH_SERV_PORT)).start();
            (new UpdateServer(ServerConstants.UPDATE_SERV_PORT)).start();
            (new PollServer(ServerConstants.POLL_SERV_PORT)).start();
            (new ChatServer(ServerConstants.CHAT_SERV_PORT)).start();
            new MemberDAO();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}