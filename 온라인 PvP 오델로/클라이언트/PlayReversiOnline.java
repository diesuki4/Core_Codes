package othello;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

/*
    게임은 서버가 실행한다.
    
    유저는 자신의 턴일때 돌을 둘 수 있고
    돌의 위치 정보를 전송하면,
    처리 결과와 현재 바둑돌의 상태를 고지 받는다.
*/

// 온라인 오델로 플레이 화면
public class PlayReversiOnline extends JPanel
{    
    private JButton btnBack;                        // 뒤로가기 버튼
    private JLabel lblWhiteCount, lblBlackCount;    // 흰돌, 검은돌의 현재 개수 표시를 위한 라벨

    private JLabel userOne;    // 유저의 아이디
    private JLabel userTwo;    // 유저의 아이디
    
    // 매칭 서버와의 연결 정보 (매칭이 완료되고 게임을 진행할 때도 이 연결을 사용한다.)
    private Socket clnt;
    private BufferedReader br;
    private PrintWriter pw;
    
    private boolean bEnd;    // 게임의 종료를 나타내는 변수
    private boolean bMyTurn;    // 자신의 턴인지 나타내는 변수
    private String myColor;;    // 나의 돌 색을 나타내는 변수
    private String myId, vsId;    // 나의 아이디, 상대의 아이디를 저장하는 변수
    
    // 서버에서 전송된 8x8 총 64 길이의 문자열로 된 바둑판의 상태를 저장하는 변수 (EEEEBWEEBBWW...)
    private String encodedStatus;
    
    // 채팅 서버와의 연결 정보
    Socket chatSock;
    BufferedReader chatBr;
    PrintWriter chatPw;
    
    // 채팅 내용이 표시되는 영역, 채팅 입력 필드
    private JTextArea txtArea;
    private JTextField txt;    
    
    public PlayReversiOnline()
    {
        setPreferredSize(new Dimension(AppManager.SCREEN_WIDTH, AppManager.SCREEN_HEIGHT));
        setBackground(new Color(57,131,66));
        setLayout(null);
        
        // 검은돌 개수, 흰돌 개수 표시를 위한 라벨들 초기화
        lblWhiteCount = new JLabel();
        lblBlackCount = new JLabel();
        lblWhiteCount.setForeground(Color.white);
        lblBlackCount.setForeground(Color.black);
        lblWhiteCount.setFont(new Font("Consolas",Font.BOLD,120));
        lblBlackCount.setFont(new Font("Consolas",Font.BOLD,120));
        lblWhiteCount.setBounds(735,20,240,180);
        lblBlackCount.setBounds(985,20,240,180);
        lblWhiteCount.setHorizontalAlignment(SwingConstants.CENTER);
        lblBlackCount.setHorizontalAlignment(SwingConstants.CENTER);
        lblWhiteCount.setVerticalAlignment(SwingConstants.CENTER);
        lblBlackCount.setVerticalAlignment(SwingConstants.CENTER);
        
        // 유저 아이디 라벨들 초기화
        userOne = new JLabel();
        userTwo = new JLabel();
        userOne.setForeground(Color.orange);
        userTwo.setForeground(Color.orange);
        userOne.setFont(new Font("Consolas", Font.BOLD, 40));
        userTwo.setFont(new Font("Consolas", Font.BOLD, 40));
        userOne.setBounds(780, 215, 240, 40);
        userTwo.setBounds(1030, 215, 240, 40);

        // 뒤로 가기 버튼
        ImageIcon image = new ImageIcon(Main.class.getResource("../images/back.png"));
        btnBack = new JButton(new ImageIcon(image.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH)));
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(new BackListener());    // 뒤로가기 버튼 리스너
        btnBack.setBounds(1050,630,200,60);
        
        /* 채팅 */
        txtArea = new JTextArea("", 10, 10);
        txtArea.setFont(new Font("SansSerif", Font.BOLD, 18));
        JScrollPane jsp = new JScrollPane(txtArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        txt = new JTextField(10);
        txt.setFont(new Font("SansSerif", Font.BOLD, 18));
        txt.addActionListener(new ChatListener());

        jsp.setBounds(670, 280, 560, 280);
        txt.setBounds(670, 580, 560, 40);
        add(jsp);
        add(txt);
        /* 채팅 */
        
        add(btnBack);
        add(lblWhiteCount);
        add(lblBlackCount);
        add(userOne);
        add(userTwo);
        addMouseListener(new BoardListener());    // 바둑판에서 클릭시 위치를 판별하고 서버로 전송하기 위한 이벤트 리스너
    } // PlayReversiOnline Constructor
    
    // 초기화
    public void start()
    {
        // 돌 개수 라벨
        lblWhiteCount.setText("0");
        lblBlackCount.setText("0");
        lblWhiteCount.setBorder(null);
        lblBlackCount.setBorder(null);
        
        // 유저 아이디 라벨
        userOne.setText("User 1");
        userTwo.setText("User 2");
        
        // 채팅
        txt.setText("");
        txtArea.setText("");
        txt.setEnabled(false);
        txtArea.setEditable(false);
        
        // 초기에는 바둑판 외에 아무것도 그리지 않기 위함
        encodedStatus = "";
        for (int i=1; i<=Reversi.BOARD_SIZE; i++)
            encodedStatus += "EEEEEEEE";
        
        vsId = "";
        bEnd = true;
        repaint();
        
        chatSock = null;
        chatBr = null;
        chatPw = null;
        
        // 매칭 서버의 대기열에 등록 요청
        if (connectServer())
        {    // 등록이 성공적으로 완료됨
            
            // 이 쓰레드는 매칭이 완료된 이후, 게임의 진행시
            // 서버에서 전송되는 처리 결과, 바둑판의 상태를 기준으로
            // 화면, 라벨, 변수들의 갱신을 담당한다.
            (new Thread() {
                @Override
                public void run()
                {
                    try
                    {
                        myId = AppManager.getInstance().getUserInfo().getId();
                        // 매칭이 완료되면, 서버가
                        // 상대방의 아이디, 나의 돌색, 바둑판의 초기 상태를 전송한다
                        vsId = Encryptor.decrypt(br.readLine());
                        if (vsId == null)
                        {    // 매칭이 되기전에 내가 뒤로 가기 키를 누른 경우, 쓰레드 종료
                            interrupt();
                            return;
                        }
                        
                        myColor = br.readLine();
                        encodedStatus = br.readLine();
                
                        userOne.setText((myColor.equals(ServerConstants.MATCH_WHITE) ? myId : vsId));
                        userTwo.setText((myColor.equals(ServerConstants.MATCH_BLACK) ? myId : vsId));
                        
                        bEnd = false;                        
                        
                        // 검정색 돌이 먼저 돌을 둔다
                        if (myColor.equals(ServerConstants.MATCH_BLACK))
                            bMyTurn = true;
                        else if (myColor.equals(ServerConstants.MATCH_WHITE))
                            bMyTurn = false;
                        
                        {    /* 채팅 */
                            try
                            {
                                // 채팅 서버에 연결 요청
                                chatSock = new Socket(ServerConstants.SERVER_DOMAIN, ServerConstants.CHAT_SERV_PORT);
                                chatBr = new BufferedReader(new InputStreamReader(chatSock.getInputStream(), "UTF8"));
                                chatPw = new PrintWriter(new OutputStreamWriter(chatSock.getOutputStream(), "UTF8"));
                                
                                // 나의 아이디, 상대의 아이디 전송
                                chatPw.println(Encryptor.encrypt(myId));
                                chatPw.println(Encryptor.encrypt(vsId));
                                chatPw.flush();
                                
                                // 채팅 입력창 활성화
                                txt.setEnabled(true);
                                txtArea.append("[서버] " + Encryptor.decrypt(chatBr.readLine()) + " 님이 입장하셨습니다." + System.lineSeparator());
                                txtArea.setCaretPosition(txtArea.getDocument().getLength());
                                txtArea.append("[서버] " + Encryptor.decrypt(chatBr.readLine()) + " 님이 입장하셨습니다." + System.lineSeparator());
                                txtArea.setCaretPosition(txtArea.getDocument().getLength());
                                
                                // 이 쓰레드는 서버가 보낸 상대방의 내용을 채팅 영역에 추가해주는 역할을 한다.
                                (new Thread(){
                                    @Override
                                    public void run()
                                    {
                                        try {
                                            while (true) {
                                                txtArea.append(vsId + " > " + chatBr.readLine() + System.lineSeparator());
                                                txtArea.setCaretPosition(txtArea.getDocument().getLength());
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        interrupt();
                                    }
                                }).start();
                            }    /* 채팅 try */ 
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }    /* 채팅 */
                        
                        // 초기 상태 설정, 채팅 쓰레드 생성을 마친후 화면을 갱신
                        updateLabels();
                        repaint();

                        (new Music("match.mp3", false)).start();
                        // 게임이 끝나지 않은 동안 실행
                        while (!bEnd)
                        {
                            // 처리 결과와 현재 바둑판의 상태
                            String result = br.readLine();
                            encodedStatus = br.readLine();
            
                            if (result.equals(ServerConstants.MATCH_OK))
                            {    // 그곳에 돌을 둘 수 있어 처리했다
                                (new Music("disc.mp3", false)).start();
                                bMyTurn = false;    // 나의 차례가 아님
                                updateLabels();
                                repaint();
                            }
                            else if (result.equals(ServerConstants.MATCH_YOUR_TURN))
                            {    // 너의 차례이다
                                bMyTurn = true;    // 나의 차례
                                updateLabels();
                                repaint();
                            }
                            else if (result.equals(ServerConstants.MATCH_CANT_PUT))
                            {    // 그곳에 돌을 둘 수 없다
                                (new Music("error.mp3", false)).start();
                                updateLabels();
                                repaint();
                                System.out.println("그곳에 놓을 수 없음");
                                JOptionPane.showMessageDialog(PlayReversiOnline.this, "Can't put disc to there.");
                            }
                            else if (result.equals(ServerConstants.MATCH_PASS))
                            {    // 돌을 둘곳이 없어 패스해야 한다
                                (new Music("error.mp3", false)).start();
                                updateLabels();
                                repaint();
                                System.out.println("너는 패스이다");
                                JOptionPane.showMessageDialog(PlayReversiOnline.this, "You can't put disc anywhere. Pass.");
                            }
                            else if (result.equals(ServerConstants.MATCH_VS_PASS))
                            {    // 상대방이 돌을 둘 곳이 없어 패스해야 한다
                                (new Music("error.mp3", false)).start();
                                updateLabels();
                                repaint();
                                System.out.println("상대가 패스이다");
                                JOptionPane.showMessageDialog(PlayReversiOnline.this, "He can't put disc anywhere. Pass.");
                            }
                            else if (result.equals(ServerConstants.MATCH_WIN) ||
                                    result.equals(ServerConstants.MATCH_LOSE))
                            {    // 이기거나 진 경우
                                updateLabels();
                                repaint();
                                bEnd = true;    // 게임이 종료됨
                                handleEnd(result);    // 종료후 작업 처리
                            }
                            else if (result.equals(ServerConstants.MATCH_ESCAPE))
                            {    // 상대가 탈주한 경우
                                // ServerConstants 클래스의 34~39 라인 참고
                                pw.println(ServerConstants.MATCH_ALIVE);
                                pw.flush();
                                
                                updateLabels();
                                repaint();
                                bEnd = true;    // 게임이 종료됨
                                handleEnd(result);    // 종료후 작업 처리
                            }
                        }
                    }    // 첫번째 try
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    finally
                    {
                        interrupt();
                    }
                }    // 첫번째 run()
            }).start();    // 첫번째 new Thread()
        }    // if (connectServer())
    }    // start()
    
    // 매칭 서버의 대기열에 등록 요청
    private boolean connectServer()
    {
        String result = "";
        
        try
        {
            // 매칭 서버의 대기열에 등록 요청
            clnt = new Socket(ServerConstants.SERVER_DOMAIN, ServerConstants.MATCH_SERV_PORT);
            br = new BufferedReader(new InputStreamReader(clnt.getInputStream(), "UTF8"));
            pw = new PrintWriter(new OutputStreamWriter(clnt.getOutputStream(), "UTF8"));
            
            // 나의 아이디와 로그인시 받은 정수키 전송
            pw.println(Encryptor.encrypt(AppManager.getInstance().getUserInfo().getId()));
            pw.println(Encryptor.encrypt(AppManager.getInstance().getUserInfo().getKey()));
            pw.flush();

            result = br.readLine();
            if (result.equals(ServerConstants.MATCH_INVALID))    // 계정이 유효하지 않음
                System.out.println("유효하지 않은 로그인");
            else if (result.equals(ServerConstants.MATCH_VALID))    // 계정이 유효함
                System.out.println("유효한 로그인");
            
            if (result.equals(ServerConstants.MATCH_INVALID))    // 계정이 유효하지 않은 경우, 매칭 서버와 연결 종료
            {    
                br.close();
                pw.close();
                clnt.close();
                return false;
            }
            
            return true;    // 계정이 유효한 경우
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }    // connectServer()

    // 바둑판, 바둑알 그리기
    public void paintComponent(Graphics page)
    {
        Graphics2D g2d = (Graphics2D) page; // 선의 두께를 변경하기 위함
        super.paintComponent(page);
        
        page.setColor(new Color(128,64,64));
        g2d.setStroke(new BasicStroke(6f));    // 선 두께 설정

        // 안티 앨리어싱 적용
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        
        // 8*8 바둑판 그리기
        for (int i=1; i<=9; i++)
        {
            page.drawLine(70,70*i,630,70*i);
            page.drawLine(70*i,70,70*i,630);
        }
        
        // 현재 상태를 바탕으로 바둑알 그리기
        g2d.setStroke(new BasicStroke(4f));    // 바둑알 테두리의 두께
        
        int k = 0;
        for (int row=1; row<=Reversi.BOARD_SIZE; row++)
            for (int col=1; col<=Reversi.BOARD_SIZE; col++)
                // 서버에서 전송받은 바둑판의 상태를 기준으로 그림
                drawDisc(page, row, col, encodedStatus.charAt(k++));
        
    } // paintComponent()
    
    // 바둑판의 해당 (행, 열) 에 BLACK/WHITE 바둑알을 그리는 함수 
    public void drawDisc(Graphics page, int row, int col, char status)
    {
        // Status 는 EMPTY 등도 정의하고 있기 때문에 WHITE/BLACK 일때만 그림
        if (status==ServerConstants.MATCH_CHAR_WHITE || status==ServerConstants.MATCH_CHAR_BLACK)
        {
            // 바둑알 그리기
            page.setColor((status==ServerConstants.MATCH_CHAR_WHITE ? Color.white : Color.black));
            page.fillOval(70*row+6, 70*col+6, 58, 58);
            // 바둑알 테두리 그리기
            page.setColor(Color.black);
            page.drawOval(70*row+6, 70*col+6, 58, 58);
        }
    } // drawDisc()
    
    // 현재 바둑돌의 개수를 갱신하고, 현재 차례의 유저의 라벨에 보더를 생성하는 함수
    public void updateLabels()
    {
        int wCount = 0, bCount = 0;
        System.out.println(encodedStatus);
        // 서버에서 전송받은 바둑판의 상태를 기준으로 돌 개수 갱신
        for (int i=0; i<64; i++)
            if (encodedStatus.charAt(i) == ServerConstants.MATCH_CHAR_BLACK)
                bCount++;
            else if (encodedStatus.charAt(i) == ServerConstants.MATCH_CHAR_WHITE)
                wCount++;
        
        lblWhiteCount.setText(Integer.toString(wCount));
        lblBlackCount.setText(Integer.toString(bCount));
        
        if (myColor.equals(ServerConstants.MATCH_BLACK))
        {
            if (bMyTurn)
            {    // 내가 검정색이고 내 턴이면
                lblBlackCount.setBorder(BorderFactory.createLineBorder(new Color(234, 204, 26), 10, true));
                lblWhiteCount.setBorder(null);
            }
            else
            {    // 내가 검정색이고 내 턴이 아니면
                lblBlackCount.setBorder(null);
                lblWhiteCount.setBorder(BorderFactory.createLineBorder(new Color(234, 204, 26), 10, true));
            }
        }
        else if (myColor.equals(ServerConstants.MATCH_WHITE))
        {
            if (bMyTurn)
            {    // 내가 하얀색이고 내 턴이면
                lblBlackCount.setBorder(null);
                lblWhiteCount.setBorder(BorderFactory.createLineBorder(new Color(234, 204, 26), 10, true));
            }
            else
            {    // 내가 하얀색이고 내 턴이 아니면
                lblBlackCount.setBorder(BorderFactory.createLineBorder(new Color(234, 204, 26), 10, true));
                lblWhiteCount.setBorder(null);
            }
        }
    } // updateLabels()
    
    // 게임 종료시 다음 수행을 선택하기 위한 함수
    public void handleEnd(String result)
    {
        try
        {
            // 매칭 서버와의 연결 종료
            br.close();
            pw.close();
            clnt.close();

            // 채팅 서버와의 연결 종료
            chatPw.close();
            chatBr.close();
            chatSock.close();

            // 채팅 입력 불가
            txt.setEnabled(false);
            
            // 승자가 전적 갱신 서버에 전적 갱신을 요청하게 된다.
            if (!result.equals(ServerConstants.MATCH_LOSE))
            {
                // 비겼을 시, 검정색 유저가 전적 갱신을 요청하게 된다.
                if (!result.equals(ServerConstants.MATCH_DRAW) ||
                    (result.equals(ServerConstants.MATCH_DRAW) && myColor.equals(ServerConstants.MATCH_BLACK)))
                {
                    // 전적 갱신 서버에 처리 요청
                    clnt = new Socket(ServerConstants.SERVER_DOMAIN, ServerConstants.UPDATE_SERV_PORT);
                    br = new BufferedReader(new InputStreamReader(clnt.getInputStream(), "UTF8"));
                    pw = new PrintWriter(new OutputStreamWriter(clnt.getOutputStream(), "UTF8"));        
                    
                    // 내 아이디, 내 정수키, 상대 아이디, 게임 결과 전송
                    pw.println(Encryptor.encrypt(myId));
                    pw.println(Encryptor.encrypt(AppManager.getInstance().getUserInfo().getKey()));
                    pw.println(Encryptor.encrypt(vsId));
                    pw.println(result);
                    pw.flush();
                    
                    // 결과 출력
                    System.out.println(br.readLine());
    
                    // 전적 갱신 서버와 연결 종료
                    br.close();
                    pw.close();
                    clnt.close();
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // 게임의 승패를 표시하고, 또 플레이할 것인지 선택하는 메시지 박스를 띄움
        String rlt = "";
        
        // 내가 이겼거나 상대가 탈주한 경우
        if (result.equals(ServerConstants.MATCH_WIN) || result.equals(ServerConstants.MATCH_ESCAPE)) {
            (new Music("select.mp3", false)).start();
            rlt = "You win !!";
        }
        // 내가 진 경우
        else if (result.equals(ServerConstants.MATCH_LOSE)) {
            (new Music("lose.mp3", false)).start();
            rlt = "You Lose.";
        }
        // 비긴 경우
        else if (result.equals(ServerConstants.MATCH_DRAW)) {
            (new Music("select.mp3", false)).start();
            rlt = "Draw !!";    
        }
        
        int select = JOptionPane.showConfirmDialog(this,
                                rlt + System.lineSeparator() + "Retry?",
                                    "Game finished", JOptionPane.YES_NO_OPTION);
    
        if (select == JOptionPane.YES_OPTION)
        {
            // 초기화
            start();
        }
        // 아니오를 누르거나 메시지 박스를 그냥 닫으면 아무 작업도 하지 않음
        
    } // handlEnd()
    
    // 뒤로가기 버튼을 위한 리스너
    public class BackListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent evt)
        {
            if (evt.getSource() == btnBack)
            {
                (new Music("back.mp3",false)).start(); // 클릭 사운드 이펙트
                
                // 게임이 끝난 상태(아직 매칭이 안되었거나 플레이후 게임이 끝난 경우)에서 뒤로가기 버튼을 누를 경우
                if (bEnd)
                {
                    // 마이 페이지로 화면 전환
                    AppManager.getInstance().getMainFrame().switchPanel(PlayReversiOnline.this, AppManager.getInstance().getSettingPanel());
                
                    // 아직 매칭이 완료되지 않아 게임을 안한 상태에서 뒤로가기를 누른 경우
                    if (vsId.equals(""))
                    {
                        try
                        {
                            // 매칭 서버의 대기열에서의 삭제를 요청
                            Socket s = new Socket(ServerConstants.SERVER_DOMAIN, ServerConstants.POLL_SERV_PORT);
                            BufferedReader b = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF8"));
                            PrintWriter p = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF8"));
                            
                            // 내 아이디, 정수키를 전송
                            p.println(Encryptor.encrypt(myId));
                            p.println(Encryptor.encrypt(AppManager.getInstance().getUserInfo().getKey()));
                            p.flush();
                            
                            if (b.readLine().equals(ServerConstants.POLL_SUCCESS))    // 매칭 서버의 대기열에서 삭제 완료
                                System.out.println("대기열에서 제거 완료");
                            else if (b.readLine().equals(ServerConstants.POLL_FAIL))    // 매칭 서버의 대기열에서 삭제 실패
                                System.out.println("대기열에서 제거 실패");
                            
                            // Poll Server 와 연결 종료
                            b.close();
                            p.close();
                            s.close();
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                    
                    try
                    {
                        // 매칭 서버와 연결 종료
                        pw.close();
                        br.close();
                        clnt.close();
                        
                        // 채팅 서버와 연결 종료
                        if (chatSock != null)
                        {
                            chatPw.close();
                            chatBr.close();
                            chatSock.close();
                        }
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                // 게임 도중에 뒤로가기 버튼을 클릭하면
                else
                {    // 정말 뒤로가기 할것인지 한번 물어봄
                    int select = JOptionPane.showConfirmDialog(PlayReversiOnline.this,
                                                            "Are you sure?",
                                                            "Back to my page", JOptionPane.YES_NO_OPTION);
                    
                    if (select == JOptionPane.YES_OPTION)
                    {
                        // 마이 페이지로 화면 전환
                        AppManager.getInstance().getMainFrame().switchPanel(PlayReversiOnline.this, AppManager.getInstance().getSettingPanel());
                        
                        try
                        {
                            // 매칭 서버와 연결 종료
                            pw.close();
                            br.close();
                            clnt.close();
                            
                            // 채팅 서버와 연결 종료
                            chatPw.close();
                            chatBr.close();
                            chatSock.close();
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } // actionPerformed()
    } // BackListener Inner Class
    
    // 바둑판에서 이벤트가 발생하면 좌표를 이용해 (행, 열) 을 판별하고 처리를 수행하기 위한 이너 클래스
    public class BoardListener implements MouseListener
    {
        @Override
        public void mouseClicked(MouseEvent evt) {}
        @Override
        public void mousePressed(MouseEvent evt) {}
        @Override
        public void mouseEntered(MouseEvent evt) {}
        @Override
        public void mouseExited(MouseEvent evt) {}
        
        @Override
        public void mouseReleased(MouseEvent evt)
        {
            Point pt = evt.getPoint();
            // 이벤트가 발생한 곳의 좌표를 통해 (행, 열) 정보를 추출
            int row = pt.x / 70;
            int col = pt.y / 70;
            
            // 좌표가 바둑판을 벋어나거나 게임이 끝난 상태, 내 턴이 아니라면 아무것도 안함
            if ( (row<1 || 8<row) || (col<1 || 8<col) || bEnd || !bMyTurn)
                return;

            // 매칭 서버에 (행, 열) 정보 전송
            pw.println(Integer.toString(row*10 + col));
            pw.flush();
        } // mouseReleased()
    } // BoardListener Inner Class
    
    public class ChatListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == txt)
            {
                // 내가 입력한 내용은 내가 채팅 영역에 갱신함
                txtArea.append(myId + " > " + txt.getText() + System.lineSeparator());
                txtArea.setCaretPosition(txtArea.getDocument().getLength());
                // 채팅 서버에 내가 입력한 내용 전송
                chatPw.println(txt.getText());
                chatPw.flush();
                txt.setText("");
            }
        } // actionPerformed(ActionEvent e)
    } // ChatListener Inner Class
} // PlayReversiOnline Class
