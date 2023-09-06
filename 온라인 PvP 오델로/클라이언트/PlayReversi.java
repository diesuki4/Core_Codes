package othello;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*
    초기 상태는 가운데에
    (백)(흑)
    (흑)(백)
    인 상태에서 시작하게 된다.
    
    사용자는 검은돌, 컴퓨터는 흰돌로 플레이하게 되며
    검은돌인 사용자가 먼저 돌을 두게 된다.
*/

// 오델로 플레이 화면
public class PlayReversi extends JPanel
{    
    private Reversi reversi;                        // 내부적으로 오델로를 실행하는 코어 (함수들의 반환값을 이용하여 화면에 표시)
    
    private JButton btnBack;                        // 뒤로가기 버튼
    private JLabel lblLevel;                        // 레벨 표시를 위한 라벨
    private JLabel lblWhiteCount, lblBlackCount;    // 흰돌, 검은돌의 현재 개수 표시를 위한 라벨
    
    public PlayReversi()
    {
        setPreferredSize(new Dimension(AppManager.SCREEN_WIDTH, AppManager.SCREEN_HEIGHT));
        setBackground(new Color(57,131,66));
        setLayout(null);

        reversi = null;    // 레벨이 선택되면 init(Level) 함수에서 초기화하게 된다
        
        // 레벨, 검은돌 개수, 흰돌 개수 표시를 위한 라벨들 초기화
        lblLevel = new JLabel();
        lblWhiteCount = new JLabel();
        lblBlackCount = new JLabel();
        
        lblLevel.setForeground(new Color(221,134,117));
        lblWhiteCount.setForeground(Color.white);
        lblBlackCount.setForeground(Color.black);
        
        lblLevel.setFont(new Font("Consolas",Font.BOLD,80));
        lblWhiteCount.setFont(new Font("Consolas",Font.BOLD,120));
        lblBlackCount.setFont(new Font("Consolas",Font.BOLD,120));
        
        lblLevel.setBounds(800,20,360,160);
        lblWhiteCount.setBounds(735,160,240,180);
        lblBlackCount.setBounds(985,160,240,180);
        
        lblLevel.setHorizontalAlignment(SwingConstants.CENTER);
        lblWhiteCount.setHorizontalAlignment(SwingConstants.CENTER);
        lblBlackCount.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblLevel.setVerticalAlignment(SwingConstants.CENTER);
        lblWhiteCount.setVerticalAlignment(SwingConstants.CENTER);
        lblBlackCount.setVerticalAlignment(SwingConstants.CENTER);
        
        //뒤로가기 버튼 이미지 씌우기 //
        ImageIcon image = new ImageIcon(Main.class.getResource("../images/back.png"));
        btnBack = new JButton(new ImageIcon(image.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH)));
        // 경계 제거
        btnBack.setBorderPainted(false);
        // 내용 제거
        btnBack.setContentAreaFilled(false);
        // 포커스시 경계 제거
        btnBack.setFocusPainted(false);
        
        btnBack.addActionListener(new BackListener());    // 뒤로가기 버튼 리스너
        btnBack.setBounds(1050,630,200,60);
        add(btnBack);
        add(lblLevel);
        add(lblWhiteCount);
        add(lblBlackCount);
        addMouseListener(new BoardListener());    // 바둑판에서 클릭시 위치를 판별하고 처리를 수행하기 위한 리스너
    } // PlayReversi Constructor
    
    public void paintComponent(Graphics page)
    {
        // 바둑판, 바둑알 그리기
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
        
        for (int row=1; row<=Reversi.BOARD_SIZE; row++)
            for (int col=1; col<=Reversi.BOARD_SIZE; col++)
                drawDisc(page, row, col, reversi.statusAt(row, col));
        
    } // paintComponent()
    
    // 바둑판의 해당 (행, 열) 에 BLACK/WHITE 바둑알을 그리는 함수 
    public void drawDisc(Graphics page, int row, int col, Status status)
    {
        // Status 는 EMPTY 등도 정의하고 있기 때문에 WHITE/BLACK 일때만 그림
        if (status==Status.WHITE || status==status.BLACK)
        {
            // 바둑알 그리기
            page.setColor((status==Status.WHITE ? Color.white : Color.black));
            page.fillOval(70*row+6, 70*col+6, 58, 58);
            // 바둑알 테두리 그리기
            page.setColor(Color.black);
            page.drawOval(70*row+6, 70*col+6, 58, 58);
        }
    } // drawDisc()
    
    // 현재 차례의 개수 표시 라벨에 보더를 생성하는 함수
    public void highlightLabel()
    {
        if (reversi.getTurn() == Status.WHITE)
        {
            lblWhiteCount.setBorder(BorderFactory.createLineBorder(new Color(234, 204, 26), 10, true));
            lblBlackCount.setBorder(null);
        }
        else
        {
            lblWhiteCount.setBorder(null);
            lblBlackCount.setBorder(BorderFactory.createLineBorder(new Color(234, 204, 26), 10, true));
        }
    } // highlightLabel()
    
    // 플레이 전 초기화하는 함수
    // LevelPanel 에서 레벨이 선택되고난 후 PanelReversi 에서 호출하는 함수
    public void init(Level level)
    {
        // 알맞은 레벨로 코어 초기화
        reversi = new Reversi(level);
        (new Music("match.mp3", false)).start();
        
        lblLevel.setText(reversi.getLevel().toString());
        lblWhiteCount.setText("2");
        lblBlackCount.setText("2");
        
        highlightLabel();
    } // init()
    
    // 게임 종료시 다음 수행을 선택하기 위한 함수
    public void handleEnd()
    {
        String result;
        
        if (reversi.getBlackCount() > reversi.getWhiteCount()) {
            (new Music("select.mp3", false)).start();
            result = "Player win !!";
        }
        else if (reversi.getBlackCount() < reversi.getWhiteCount()) {
            (new Music("lose.mp3", false)).start();
            result = "Computer win :(";
        }
        else {
            (new Music("select.mp3", false)).start();
            result = "Draw !!";
        }
        
        // 게임의 승패를 표시하고, 재시도할 것인지 선택하는 메시지 박스를 띄움
        int select = JOptionPane.showConfirmDialog(this, result + System.lineSeparator() + "Retry?",
                                                    "Game finished", JOptionPane.YES_NO_OPTION);
    
        if (select == JOptionPane.YES_OPTION)
        {
            // 초기화 후 화면 다시 그리기
            init(reversi.getLevel());
            repaint();
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
                // 게임이 끝난 상태에서 뒤로가기 버튼을 누를 경우 (handleEnd 함수에서 아니오나 대화상자를 닫은 경우)
                if (reversi.isEnd()) {
                    AppManager.getInstance().getMainFrame().switchPanel(PlayReversi.this, AppManager.getInstance().getLevelPanel());
                    (new Music("back.mp3",false)).start(); // 클릭 사운드 이펙트
                }
                // 게임 도중에 뒤로가기 버튼을 클릭하면
                else
                {    // 정말 뒤로가기 할것인지 한번 물어봄
                    (new Music("select.mp3",false)).start();
                    int select = JOptionPane.showConfirmDialog(PlayReversi.this,
                                                            "Are you sure?",
                                                            "Back to level selection", JOptionPane.YES_NO_OPTION);
                    
                    if (select == JOptionPane.YES_OPTION) {
                        (new Music("back.mp3",false)).start();
                        AppManager.getInstance().getMainFrame().switchPanel(PlayReversi.this, AppManager.getInstance().getLevelPanel());
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
        
        // 플레이의 주도자가 유저이기 때문에(유저가 돌을 놓아야 컴퓨터가 돌을 놓기 때문에)
        // 컴퓨터가 돌을 놓는 것을 처리하는 부분까지 mouseReleased(MouseEvent) 함수에 포함시켜야 함.
        @Override
        public void mouseReleased(MouseEvent evt)
        {
            Point pt = evt.getPoint();
            // 이벤트가 발생한 곳의 좌표를 통해 (행, 열) 정보를 추출
            int row = pt.x / 70;
            int col = pt.y / 70;
            
            // 좌표가 바둑판을 벋어나거나 게임이 끝난 상태라면 아무것도 안함
            if ( (row<1 || 8<row) || (col<1 || 8<col) || reversi.isEnd() )
                return;
            
            // 돌을 놓을 수 있는 경우 내부적으로 실행 및 업데이트한 후 true
            // 놓을 수 없는 경우 false
            if (reversi.handleInput(row, col))
            {
                (new Music("disc.mp3",false)).start(); // 돌 놓을시 사운드 이펙트
                repaint(); // 바둑판 다시 그림
                
                // 라벨에 표시되는 돌의 개수 갱신
                lblWhiteCount.setText(Integer.toString(reversi.getWhiteCount()));
                lblBlackCount.setText(Integer.toString(reversi.getBlackCount()));
                
                // 게임이 종료되었을 경우 true (두 플레이어 모두 돌을 둘 곳이 없을 경우)
                if (reversi.isEnd())
                {
                    handleEnd();
                    return;
                }
                
                // 현재 차례의 라벨에 보더 생성
                highlightLabel();
                
                // 현재 차례의 플레이어가 돌을 놓을 수 있는 곳이 없을 경우 true (현재 Computer 의 차례)
                if (reversi.isPass())
                {
                    (new Music("error.mp3",false)).start(); // 사운드 이펙트
                    JOptionPane.showMessageDialog(PlayReversi.this, "Computer can't put disc anywhere. Pass.");
                    reversi.toggleTurn();    // 차례를 변경 (흰돌 -> 흑돌) (컴퓨터 -> 유저)
                    highlightLabel();    // 현재 차례의 개수 라벨 보더 생성
                }
                else // 컴퓨터가 둘 곳이 있을 경우
                {
                    while (true) // 컴퓨터가 돌을 놓고 업데이트 했을때, 유저가 놓을 수 있는 곳이 없는 경우가 지속적으로 발생할 수 있기 때문 
                    {
                        // Reversi의 객체 생성시 인자로 넘겨준 level 을 이용하여 레벨에 알맞은 위치를 결정하여 반환 
                        Position computerPosition = reversi.positionGenerator();
                        
                        // 3~6 초간 랜덤으로 컴퓨터의 턴 딜레이하는 부분 //
                        
                        /*
                            Thread.sleep() 등의 함수를 사용할 경우 위의 repaint() 가 실행되지 않는 등의
                            문제가 있어, 메시지 박스를 띄우면 위의 repaint() 함수가 실행된다는 점을 이용.
                            메시지 박스를 띄우고, Timer 에서 설정한 시간이 지나면 dispose 시키는 방식으로 작동.
                            메시지 박스의 좌표가 (-1000, -1000) 이므로 화면에 보이지는 않음.
                        */
                        
                        JDialog jd = (new JOptionPane("",JOptionPane.PLAIN_MESSAGE)).createDialog("");
                        jd.setLocation(-1000, -1000);
                        
                        Timer timer = new Timer(1, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) { jd.dispose(); }
                        });
                        
                        //timer.setInitialDelay(((int)(Math.random()*4) + 3) * 1000); // 컴퓨터를 기다리는 시간을 조정하려면 ms 단위로 입력하세요
                        timer.setInitialDelay(500);
                        timer.setRepeats(false);
                        timer.start();
                        jd.setVisible(true);
                        
                        // 3~6 초간 랜덤으로 컴퓨터의 턴 딜레이하는 부분 //

                        // 컴퓨터가 결정한 위치를 이용하여 내부적으로 처리
                        reversi.handleInput(computerPosition.getRow(), computerPosition.getCol());
                        (new Music("disc.mp3",false)).start(); // 사운드 이펙트
                        
                        repaint();
                        lblWhiteCount.setText(Integer.toString(reversi.getWhiteCount()));
                        lblBlackCount.setText(Integer.toString(reversi.getBlackCount()));
                        
                        // 게임이 종료되었을 경우 true (두 플레이어 모두 돌을 둘 곳이 없을 경우)
                        if (reversi.isEnd())
                        {
                            handleEnd();
                            break;
                        }
                        
                        // 현재 차례의 개수 라벨 보더 생성
                        highlightLabel();
                        
                        // 현재 차례의 플레이어가 돌을 놓을 수 있는 곳이 없을 경우 true (현재 유저의 차례)
                        if (reversi.isPass())
                        {
                            (new Music("error.mp3",false)).start(); // 사운드 이펙트
                            JOptionPane.showMessageDialog(PlayReversi.this, "Player can't put disc anywhere. Pass.");
                            reversi.toggleTurn();    // 차례를 변경 (흑돌 -> 백돌) (유저 -> 컴퓨터)
                            highlightLabel(); // 현재 차례의 개수 라벨 보더 생성
                        }
                        else // 유저가 돌을 놓을 수 있는 위치가 있을 경우
                        {
                            break; // while 루프를 종료하고 mouseReleased() 가 종료됨
                        }
                    } // while
                }
            }
            else // 선택한 위치가 돌을 놓을 수 없는 위치인 경우
            {
                (new Music("error.mp3",false)).start(); // 사운드 이펙트
                JOptionPane.showMessageDialog(PlayReversi.this, "Can't put disc to there.");
            }
        } // mouseReleased()
    } // BoardListener Inner Class
} // PlayReversi Class
