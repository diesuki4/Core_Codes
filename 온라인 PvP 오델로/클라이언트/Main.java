package othello;

public class Main
{
    public static void main(String[] args)
    {    
        // 접속할 서버를 임의로 설정할 수 있다
        if (1 < args.length || (args.length == 1 && args[0].equals("-help")))
        {
            System.out.println("USAGE 1 : java -jar *.jar");
            System.out.println("USAGE 2 : java -jar *.jar <SERVER_DOMAIN>");
            return;        
        }
        else
        {
            System.out.println("Execute with '-help' argument to see other usages.");        
            
            if (args.length == 1)
            {
                ServerConstants.SERVER_DOMAIN = args[0];
                System.out.println("[SERVER] " + args[0]);
            }
        }
        
        AppManager.getInstance().setMainFrame(new MainFrame());
        AppManager.getInstance().setSelectPanel(new GameSelection());
        AppManager.getInstance().setLevelPanel(new LevelReversi());
        AppManager.getInstance().setPlayPanel(new PlayReversi());
        AppManager.getInstance().setLoginPanel(new Login());
        AppManager.getInstance().setJoinPanel(new Join());
        AppManager.getInstance().setSettingPanel(new Setting());
        AppManager.getInstance().setMyProfilePanel(new MyProfile());
        AppManager.getInstance().setOtherProfilePanel(new OtherProfile());
        AppManager.getInstance().setPlayOnlinePanel(new PlayReversiOnline());
        AppManager.getInstance().setUserInfo(new InUseInfo());
        
        AppManager.getInstance().getMainFrame().switchPanel(null, AppManager.getInstance().getSelectPanel());
        AppManager.getInstance().getMainFrame().startUI();
    }
}
