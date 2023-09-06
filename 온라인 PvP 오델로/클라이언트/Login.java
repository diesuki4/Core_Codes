package othello;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

// 로그인 화면
public class Login extends JPanel
{
   private JButton btnBack;      // 뒤로가기 버튼
   private JButton btnLogin;     // 로그인 버튼
   private JButton btnJoin;         // 회원 가입 패널로 이동하는 버튼
   private JPasswordField password;    // 패스워드 입력창
   private JTextField userID;     // 아이디 입력창
   private JLabel lblUserID;     // ID 라벨
   private JLabel lblUserPW;     // PW 라벨
   private JPanel loginPanel;     // 메인 패널
   private JPanel imgPanel;         // 조이패드 이미지 패널

   public Login()
   {
      setPreferredSize(new Dimension(AppManager.SCREEN_WIDTH, AppManager.SCREEN_HEIGHT));
      setBackground(new Color(57,131,66));
      setLayout(null);
      
     // 메인 패널
      loginPanel=new JPanel();
      loginPanel.setBackground(new Color(154, 205, 50));
      loginPanel.setBounds(450,100, 400, 500);
      loginPanel.setLayout(null);     
      
      // 조이패드 이미지 패널
      imgPanel = new JPanel();
      imgPanel.setLayout(null);
      imgPanel.setBounds(10, 10, 380, 200);
      imgPanel.setBackground(new Color(154,205,50));
      loginPanel.add(imgPanel);
      imgPanel.setLayout(null);
  
      // 조이패드 이미지
      ImageIcon image = new ImageIcon(Main.class.getResource("../images/game.png"));
      Image pre = image.getImage();
      Image scale = pre.getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);     
      
      JLabel lblimage1 = new JLabel(new ImageIcon(scale));
      lblimage1.setBounds(0, 0, 380, 200);
      imgPanel.add(lblimage1);
      
      // ID 라벨
      lblUserID = new JLabel("ID");
      lblUserID.setBounds(50,230,50,25);
      lblUserID.setFont(new Font("SansSerif", Font.BOLD, 30));
      loginPanel.add(lblUserID);
     
      // PW 라벨
      lblUserPW = new JLabel("PW");
      lblUserPW.setBounds(50, 300, 50, 25);
      lblUserPW.setFont(new Font("SansSerif", Font.BOLD, 30));
      loginPanel.add(lblUserPW);
       
       // 길이 20자 제한의 아이디 입력 필드
      userID = new JTextField(20);
      userID.setDocument(new JTextFieldLimit(20));
       userID.setBounds(130, 230, 160, 30);
       loginPanel.add(userID);

       // 길이 20자 제한의 패스워드 입력 필드
       password = new JPasswordField(20);
       password.setDocument(new JTextFieldLimit(20));
       password.setBounds(130,300, 160, 30);
       loginPanel.add(password);

       // 뒤로 가기, 회원 가입, 로그인 버튼 액션 리스너
       MyActionListener listener = new MyActionListener();
       
       /* 뒤로 가기 */
        ImageIcon image1 = new ImageIcon(Main.class.getResource("../images/back.png"));
        Image pre1 = image1.getImage();
        Image scale1 = pre1.getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
        
        btnBack = new JButton(new ImageIcon(scale1));
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(listener);
        btnBack.setBounds(1050, 630, 200, 60);
        add(btnBack);
       /* 뒤로 가기 */
        
        /* 로그인 */
      ImageIcon image2 = new ImageIcon(Main.class.getResource("../images/login1.png"));
      Image pre2 = image2.getImage();
      Image scale2 = pre2.getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
      
       btnLogin = new JButton(new ImageIcon(scale2));
       btnLogin.setBorderPainted(false);
       btnLogin.setContentAreaFilled(false);
       btnLogin.setFocusPainted(false);
       btnLogin.setBounds(280,390,100,100);
       btnLogin.addActionListener(listener);
       loginPanel.add(btnLogin);
        /* 로그인 */
     
      /* 회원 가입 */
      ImageIcon image3 = new ImageIcon(Main.class.getResource("../images/add-user.png"));
      Image pre3 = image3.getImage();
      Image scale3 = pre3.getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
      
      btnJoin = new JButton(new ImageIcon(scale3));
      btnJoin.setBorderPainted(false);
      btnJoin.setContentAreaFilled(false);
      btnJoin.setFocusPainted(false);
      btnJoin.addActionListener(listener);
      btnJoin.setBounds(20,390,100,100);
      loginPanel.add(btnJoin);     
      /* 회원 가입 */ 
       
      loginPanel.setBorder(BorderFactory.createLineBorder(new Color(173, 255, 47), 10, true));
      this.add(loginPanel);
   }
   
   public class MyActionListener implements ActionListener{

      @Override
      public void actionPerformed(ActionEvent e) {
         // TODO Auto-generated method stub
          Object obj = e.getSource();
          
          if (obj == btnJoin)
          {    // 회원 가입 버튼
              (new Music("select.mp3", false)).start();
              
              // 회원 가입 화면으로 전환
             AppManager.getInstance().getMainFrame().switchPanel(Login.this, AppManager.getInstance().getJoinPanel());
             
             userID.setText("");
             password.setText("");
          }
          else if (obj == btnLogin)
          {        // 로그인 버튼
              
              // 빈 칸이 존재할시 대화 상자 출력
              if (userID.getText().equals("") && password.getText().equals("")) {
                      (new Music("error.mp3", false)).start();
                  JOptionPane.showMessageDialog(Login.this, "계정 정보를 입력해주세요.");
                  return;
              }
              else if (userID.getText().equals("")) {
                    (new Music("error.mp3", false)).start();
                  JOptionPane.showMessageDialog(Login.this, "아이디를 입력해주세요.");
                  return;
              }
              else if (password.getText().equals("")) {
                    (new Music("error.mp3", false)).start();
                  JOptionPane.showMessageDialog(Login.this, "패스워드를 입력해주세요.");
                  return;
              }
              
                try
                {
                    // 로그인 서버에 처리 요청
                    Socket s = new Socket(ServerConstants.SERVER_DOMAIN, ServerConstants.LOGIN_SERV_PORT);
                    BufferedReader b = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF8"));
                    PrintWriter p = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF8"));    
                    
                    // 아디이, 패스워드 전송
                    p.println(Encryptor.encrypt(userID.getText()));
                    p.println(Encryptor.encrypt(password.getText()));
                    p.flush();
                    String result = b.readLine();
                
                    if (result.equals(ServerConstants.LOGIN_INUSE))
                    {    // 해당 계정이 이미 사용중
                          (new Music("error.mp3", false)).start();
                        JOptionPane.showMessageDialog(Login.this, "계정이 이미 사용중입니다.");
                    
                        try
                        {
                            p.close();
                            b.close();
                            s.close();
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                    else if (result.equals(ServerConstants.LOGIN_SUCCESS))
                    {    // 로그인 성공
                          (new Music("select.mp3", false)).start();
                          
                          // 로그인 서버가 발행한 정수키 저장
                        String key = Encryptor.decrypt(b.readLine());
                        JOptionPane.showMessageDialog(Login.this, "로그인 성공.");
                        
                        AppManager.getInstance().getUserInfo().setId(userID.getText());
                        AppManager.getInstance().getUserInfo().setKey(key);
                        
                        // 로그인 서버에서는 이 연결을 이용해 입력 대기 상태의 쓰레드를 만들고
                        // 사용자가 로그 아웃/종료 하여 이 연결이 닫히면, 예외 처리를 이용해 로그 아웃으로 판별하게 된다.
                        AppManager.getInstance().getUserInfo().setSocket(s);
                        AppManager.getInstance().getUserInfo().setBr(b);
                        AppManager.getInstance().getUserInfo().setPw(p);
                        
                        // 마이 페이지 화면으로 전환
                        AppManager.getInstance().getMainFrame().switchPanel(Login.this, AppManager.getInstance().getSettingPanel());
                    
                        userID.setText("");
                        password.setText("");
                    }
                    else if (result.equals(ServerConstants.LOGIN_FAIL))
                    {    // 로그인 실패
                          (new Music("error.mp3", false)).start();
                        JOptionPane.showMessageDialog(Login.this, "계정 정보가 존재하지 않습니다.");
                            
                        try
                        {
                            p.close();
                            b.close();
                            s.close();
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }   
          }
        else if (obj == btnBack)
        {    // 뒤로가기 버튼
            (new Music("back.mp3", false)).start();
            userID.setText("");
            password.setText("");

            // 컴퓨터, 온라인 게임 선택 화면으로 전환
            AppManager.getInstance().getMainFrame().switchPanel(Login.this, AppManager.getInstance().getSelectPanel());
        }
      } // actionPerformed()
   }  // MyActionListener Inner Class
} // Login Class
