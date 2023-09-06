import java.util.ArrayList;
import java.util.Collections;

/*
    Reversi 클래스는 오델로의 실행을 내부적으로 처리하며
    GUI로 구현하기 위한 값들은 모두 메소드를 통해 전달받는다.
    (차례 또한 자동으로 바뀌므로 특별한 때를 제외하고는 명시적으로 바꿔줄 필요가 없다.)
    (특별한 때는 돌을 둘 곳이 없어 패스하는 경우)
*/

// 내부적으로 오델로를 실행하는 코어
public class Reversi {
    public static final int BOARD_SIZE = 8; // 바둑판의 크기 정의
    private Level level; // 레벨
    private Status[][] status; // 바둑판을 [n+2][n+2] 크기의 상태(Status)로 정의 (모서리는 Status.BOUND)
    private Status currentTurn; // 현재 차례
    private int whiteCount; // 현재 흰 돌의 개수
    private int blackCount; // 현재 검은 돌의 개수
    private ArrayList<Position> whiteList; // 현재 모든 흰 돌들의 위치를 저장하는 리스트
    private ArrayList<Position> blackList; // 현재 모든 검은 돌들의 위치를 저장하는 리스트
    private ArrayList<Position> whiteAvailableList; // 현재 흰 돌을 둘 수 있는 위치들을 저장하는 리스트
    private ArrayList<Position> blackAvailableList; // 현재 검은 돌을 둘 수 있는 위치들을 저장하는 리스트

    // 돌을 둘 수 있는 위치들을 저장함으로서
    // 게임 종료와 돌을 두는것의 가능 여부의 판별이 간단해지고, 컴퓨터의 위치 결정 방식을 구현하는데 용이하다.

    public Reversi()
    {
        whiteList = new ArrayList<Position>();
        blackList = new ArrayList<Position>();
        whiteAvailableList = new ArrayList<Position>();
        blackAvailableList = new ArrayList<Position>();

        status = new Status[BOARD_SIZE + 2][BOARD_SIZE + 2];

        currentTurn = Status.BLACK; // 검은 돌(유저) 부터 시작

        // 바둑판을 초기 상태로 초기화
        for (int row = 0; row < BOARD_SIZE + 2; row++)
            for (int col = 0; col < BOARD_SIZE + 2; col++)
                if (row == 0 || col == 0 || row == BOARD_SIZE + 1 || col == BOARD_SIZE + 1)
                    status[row][col] = Status.BOUND;
                else if ((row == BOARD_SIZE / 2 && col == BOARD_SIZE / 2)
                        || (row == BOARD_SIZE / 2 + 1 && col == BOARD_SIZE / 2 + 1))
                    status[row][col] = Status.WHITE;
                else if ((row == BOARD_SIZE / 2 && col == BOARD_SIZE / 2 + 1)
                        || (row == BOARD_SIZE / 2 + 1 && col == BOARD_SIZE / 2))
                    status[row][col] = Status.BLACK;
                else
                    status[row][col] = Status.EMPTY;

        whiteCount = 2;
        blackCount = 2;

        refreshList(); // 현재 돌들이 놓인 위치를 저장하는 whiteList, blackList 를 갱신
        refreshAvailableList(); // 현재 돌을 놓을 수 있는 위치를 저장하는 whiteAvailableList, blackAvailableList 를 갱신
    } // Reversi Constructor
    
    public Reversi(Level lv)
    {
        this();
        level = lv;
    } // Reversi Constructor

    // Getter/Setter
    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Status getTurn() {
        return currentTurn;
    }

    public Status statusAt(int row, int col) {
        return status[row][col];
    }

    public int getWhiteCount() {
        return whiteCount;
    }

    public int getBlackCount() {
        return blackCount;
    }

    public int getAvailableSize(Status status) {
        return (status == Status.WHITE ? whiteAvailableList.size() : blackAvailableList.size());
    }

    // 돌을 놓을 수 있는 경우, (행, 열) 정보를 이용하여 내부적으로 상태와 멤버들을 갱신
    public boolean handleInput(int row, int col) {
        if (isPutAvailable(row, col)) // 해당 위치가 돌을 놓을 수 있는 위치인지 확인
        {
            putDisc(row, col); // status[row][col] 에 돌을 놓음
            refreshStatus(row, col); // 해당 (행, 열) 에서 8방향으로 이동하며 상대의 돌을 뒤집음
            refreshList(); // 현재 돌들이 놓인 위치를 저장하는 whiteList, blackList 를 갱신
            refreshAvailableList(); // 현재 돌을 놓을 수 있는 위치를 저장하는 whiteAvailableList, blackAvailableList 를 갱신
            toggleTurn(); // 차례 변경 (currentTurn 멤버)
            return true;
        } else // 해당 위치에 돌을 둘 수 없는 경우
        {
            return false;
        }
    } // handleInput()

    // 차례 변경
    public void toggleTurn() {
        currentTurn = (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE);
    }

    // status[row][col] 에 돌을 놓음
    public void putDisc(int row, int col) {
        if (currentTurn == Status.WHITE) {
            status[row][col] = Status.WHITE;
            whiteCount++;
        } else {
            status[row][col] = Status.BLACK;
            blackCount++;
        }
    } // putDisc()

    // 현재 차례의 유저가 돌을 놓을 수 있는 위치가 없는지 확인
    public boolean isPass() {
        return ((currentTurn == Status.WHITE ? whiteAvailableList.size() : blackAvailableList.size()) == 0);
    }

    // 게임이 종료되었는지 확인 (두 플레이어 모두 둘 수 있는 위치가 없는 경우)
    public boolean isEnd() {
        return (whiteAvailableList.size() == 0 && blackAvailableList.size() == 0);
    }

    // 해당 (행, 열) 에 돌을 둘 수 있는지 확인
    public boolean isPutAvailable(int row, int col) {
        ArrayList<Position> availableList = (currentTurn == Status.WHITE ? whiteAvailableList : blackAvailableList);
        int size = availableList.size();

        // availableList에 일치하는 (행, 열) 이 있으면 true
        for (int i = 0; i < size; i++)
            if (availableList.get(i).getRow() == row && availableList.get(i).getCol() == col)
                return true;

        return false;
    } // isPutAvailable()

    // 해당 (행, 열) 에서 8방향으로 이동해보며 상대의 돌을 뒤집는 함수
    public void refreshStatus(int row, int col) {
        TraversingPosition traversal = new TraversingPosition();
        TraversingPosition selectedPosition = new TraversingPosition();

        traversal.setPosition(row, col); //
        selectedPosition.setPosition(row, col);

        // 해당 (행, 열) 에서 상대의 돌인 동안 위로 이동한다
        for (traversal.moveUp(); status[traversal.getRow()][traversal
                .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveUp())
            ;

        // 나의 색의 돌을 만나서 멈춘 것이라면, selectedPosition 이 해당 (행, 열) 로부터 traversal 가 지나왔던 경로를
        // 따라 상대의 돌을 자신의 돌로 뒤집는다.
        if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                : Status.BLACK))
            for (selectedPosition.moveUp(); !selectedPosition.equals(traversal); selectedPosition.moveUp()) {
                status[selectedPosition.getRow()][selectedPosition
                        .getCol()] = (currentTurn == Status.WHITE ? Status.WHITE : Status.BLACK);
                // 돌을 뒤집으며 개수를 갱신
                whiteCount += (currentTurn == Status.WHITE ? 1 : -1);
                blackCount += (currentTurn == Status.WHITE ? -1 : 1);
            }
        // if 문이 실행되지 않는 경우는 이동 중 Status.EMPTY(빈 칸) 이나 Status.BOUND(경계선) 을 만나, 해당 방향에서는
        // 돌을 뒤집을 수 없는 경우이다.

        /*
         * 아래 코드들은 방향만 다를뿐 하는 작업은 모두 동일하다.
         */

        traversal.setPosition(row, col);
        selectedPosition.setPosition(row, col);

        for (traversal.moveRightUp(); status[traversal.getRow()][traversal
                .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveRightUp())
            ;
        if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                : Status.BLACK))
            for (selectedPosition.moveRightUp(); !selectedPosition.equals(traversal); selectedPosition.moveRightUp()) {
                status[selectedPosition.getRow()][selectedPosition
                        .getCol()] = (currentTurn == Status.WHITE ? Status.WHITE : Status.BLACK);
                whiteCount += (currentTurn == Status.WHITE ? 1 : -1);
                blackCount += (currentTurn == Status.WHITE ? -1 : 1);
            }

        traversal.setPosition(row, col);
        selectedPosition.setPosition(row, col);

        for (traversal.moveRight(); status[traversal.getRow()][traversal
                .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveRight())
            ;
        if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                : Status.BLACK))
            for (selectedPosition.moveRight(); !selectedPosition.equals(traversal); selectedPosition.moveRight()) {
                status[selectedPosition.getRow()][selectedPosition
                        .getCol()] = (currentTurn == Status.WHITE ? Status.WHITE : Status.BLACK);
                whiteCount += (currentTurn == Status.WHITE ? 1 : -1);
                blackCount += (currentTurn == Status.WHITE ? -1 : 1);
            }

        traversal.setPosition(row, col);
        selectedPosition.setPosition(row, col);

        for (traversal.moveRightDown(); status[traversal.getRow()][traversal
                .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveRightDown())
            ;
        if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                : Status.BLACK))
            for (selectedPosition.moveRightDown(); !selectedPosition.equals(traversal); selectedPosition
                    .moveRightDown()) {
                status[selectedPosition.getRow()][selectedPosition
                        .getCol()] = (currentTurn == Status.WHITE ? Status.WHITE : Status.BLACK);
                whiteCount += (currentTurn == Status.WHITE ? 1 : -1);
                blackCount += (currentTurn == Status.WHITE ? -1 : 1);
            }

        traversal.setPosition(row, col);
        selectedPosition.setPosition(row, col);

        for (traversal.moveDown(); status[traversal.getRow()][traversal
                .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveDown())
            ;
        if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                : Status.BLACK))
            for (selectedPosition.moveDown(); !selectedPosition.equals(traversal); selectedPosition.moveDown()) {
                status[selectedPosition.getRow()][selectedPosition
                        .getCol()] = (currentTurn == Status.WHITE ? Status.WHITE : Status.BLACK);
                whiteCount += (currentTurn == Status.WHITE ? 1 : -1);
                blackCount += (currentTurn == Status.WHITE ? -1 : 1);
            }

        traversal.setPosition(row, col);
        selectedPosition.setPosition(row, col);

        for (traversal.moveLeftDown(); status[traversal.getRow()][traversal
                .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveLeftDown())
            ;
        if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                : Status.BLACK))
            for (selectedPosition.moveLeftDown(); !selectedPosition.equals(traversal); selectedPosition
                    .moveLeftDown()) {
                status[selectedPosition.getRow()][selectedPosition
                        .getCol()] = (currentTurn == Status.WHITE ? Status.WHITE : Status.BLACK);
                whiteCount += (currentTurn == Status.WHITE ? 1 : -1);
                blackCount += (currentTurn == Status.WHITE ? -1 : 1);
            }

        traversal.setPosition(row, col);
        selectedPosition.setPosition(row, col);

        for (traversal.moveLeft(); status[traversal.getRow()][traversal
                .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveLeft())
            ;
        if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                : Status.BLACK))
            for (selectedPosition.moveLeft(); !selectedPosition.equals(traversal); selectedPosition.moveLeft()) {
                status[selectedPosition.getRow()][selectedPosition
                        .getCol()] = (currentTurn == Status.WHITE ? Status.WHITE : Status.BLACK);
                whiteCount += (currentTurn == Status.WHITE ? 1 : -1);
                blackCount += (currentTurn == Status.WHITE ? -1 : 1);
            }

        traversal.setPosition(row, col);
        selectedPosition.setPosition(row, col);

        for (traversal.moveLeftUp(); status[traversal.getRow()][traversal
                .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveLeftUp())
            ;
        if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                : Status.BLACK))
            for (selectedPosition.moveLeftUp(); !selectedPosition.equals(traversal); selectedPosition.moveLeftUp()) {
                status[selectedPosition.getRow()][selectedPosition
                        .getCol()] = (currentTurn == Status.WHITE ? Status.WHITE : Status.BLACK);
                whiteCount += (currentTurn == Status.WHITE ? 1 : -1);
                blackCount += (currentTurn == Status.WHITE ? -1 : 1);
            }
    } // refreshStatus()

    // 현재 바둑판(status[][]) 을 기준으로 whiteList 와 blackList 에 현재 돌들의 위치를 갱신
    public void refreshList() {
        whiteList.clear();
        blackList.clear();

        for (int row = 1; row < BOARD_SIZE + 1; row++)
            for (int col = 1; col < BOARD_SIZE + 1; col++)
                if (status[row][col] == Status.WHITE)
                    whiteList.add(new Position(row, col));
                else if (status[row][col] == Status.BLACK)
                    blackList.add(new Position(row, col));
    } // refreshList()

    // whiteList, blackList 각각의 모든 위치들에서 8방향으로 이동해보며
    // 돌을 놓을 수 있는 위치(whiteAvailableList, blackAvailableList) 를 갱신하는 함수
    public void refreshAvailableList() {
        int size;
        int currentRow;
        int currentCol;

        TraversingPosition traversal = new TraversingPosition(); // 돌을 둘 수 있는 위치인지 확인하기 위해 8방향으로 이동시켜 보기 위한 변수
        TraversingPosition oneBlankGoPosition = new TraversingPosition(); // 해당 (행, 열) 의 바로옆 위치인지 확인하기 위한 변수

        // whiteAvailableList 를 갱신 //
        // blackAvailableList 를 갱신하는 방법도 동일하다.

        size = whiteList.size(); // 현재 바둑돌의 개수만큼 진행
        whiteAvailableList.clear(); // 새로 갱신을 위해 리스트 초기화
        for (int i = 0; i < size; i++) {
            currentRow = whiteList.get(i).getRow();
            currentCol = whiteList.get(i).getCol();

            traversal.setPosition(currentRow, currentCol);

            // 아래 if 문에서 빈칸인 경우만 확인할 경우, 바로 위의 돌이 whiteAvailableList 에 포함될 수 있기 때문에 확인하기 위함
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveUp();

            // whiteList 에서 가져온 (행, 열) 에서 흑돌인 동안 위로 이동
            for (traversal.moveUp(); status[traversal.getRow()][traversal.getCol()] == Status.BLACK; traversal.moveUp())
                ;
            // 빈칸이라서 멈추고 그곳이 해당 (행, 열)의 바로 위가 아닌 경우 돌을 놓을 수 있는 위치
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(whiteAvailableList, traversal); // 여러 돌에서 진행하므로, 중복을 줄이기 위해 같은 위치가 있는지 확인후 추가

            /*
             * 아래 코드들은 방향만 다를뿐 하는 작업은 모두 동일하다.
             */

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveRightUp();

            for (traversal.moveRightUp(); status[traversal.getRow()][traversal.getCol()] == Status.BLACK; traversal
                    .moveRightUp())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(whiteAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveRight();

            for (traversal.moveRight(); status[traversal.getRow()][traversal.getCol()] == Status.BLACK; traversal
                    .moveRight())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(whiteAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveRightDown();

            for (traversal.moveRightDown(); status[traversal.getRow()][traversal.getCol()] == Status.BLACK; traversal
                    .moveRightDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(whiteAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveDown();

            for (traversal.moveDown(); status[traversal.getRow()][traversal.getCol()] == Status.BLACK; traversal
                    .moveDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(whiteAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveLeftDown();

            for (traversal.moveLeftDown(); status[traversal.getRow()][traversal.getCol()] == Status.BLACK; traversal
                    .moveLeftDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(whiteAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveLeft();

            for (traversal.moveLeft(); status[traversal.getRow()][traversal.getCol()] == Status.BLACK; traversal
                    .moveLeft())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(whiteAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveLeftUp();

            for (traversal.moveLeftUp(); status[traversal.getRow()][traversal.getCol()] == Status.BLACK; traversal
                    .moveLeftUp())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(whiteAvailableList, traversal);
        } // whiteAvailableList 갱신 완료

        size = blackList.size();
        blackAvailableList.clear();
        for (int i = 0; i < size; i++) {
            currentRow = blackList.get(i).getRow();
            currentCol = blackList.get(i).getCol();

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveUp();

            for (traversal.moveUp(); status[traversal.getRow()][traversal.getCol()] == Status.WHITE; traversal.moveUp())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(blackAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveRightUp();

            for (traversal.moveRightUp(); status[traversal.getRow()][traversal.getCol()] == Status.WHITE; traversal
                    .moveRightUp())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(blackAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveRight();

            for (traversal.moveRight(); status[traversal.getRow()][traversal.getCol()] == Status.WHITE; traversal
                    .moveRight())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(blackAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveRightDown();

            for (traversal.moveRightDown(); status[traversal.getRow()][traversal.getCol()] == Status.WHITE; traversal
                    .moveRightDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(blackAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveDown();

            for (traversal.moveDown(); status[traversal.getRow()][traversal.getCol()] == Status.WHITE; traversal
                    .moveDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(blackAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveLeftDown();

            for (traversal.moveLeftDown(); status[traversal.getRow()][traversal.getCol()] == Status.WHITE; traversal
                    .moveLeftDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(blackAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveLeft();

            for (traversal.moveLeft(); status[traversal.getRow()][traversal.getCol()] == Status.WHITE; traversal
                    .moveLeft())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(blackAvailableList, traversal);

            traversal.setPosition(currentRow, currentCol);
            oneBlankGoPosition.setPosition(currentRow, currentCol);
            oneBlankGoPosition.moveLeftUp();

            for (traversal.moveLeftUp(); status[traversal.getRow()][traversal.getCol()] == Status.WHITE; traversal
                    .moveLeftUp())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == Status.EMPTY && !oneBlankGoPosition.equals(traversal))
                pushElement(blackAvailableList, traversal);
        } // blackAvailableList 갱신 완료
    } // refreshStatus()

    // 컴퓨터의 레벨에 따라 적절한 위치를 결정해 반환하는 함수
    public Position positionGenerator() {
        Position computerPosition = null;

        switch (level) {
        case EASY:
            computerPosition = positionGeneratorEasy();
            break;
        case NORMAL:
            computerPosition = positionGeneratorNormal();
            break;
        case HARD:
            computerPosition = positionGeneratorHard();
            break;
        case EXPERT:
            computerPosition = positionGeneratorExpert();
            break;
        case MASTER:
            computerPosition = positionGeneratorMaster();
            break;
        }

        return computerPosition;
    } // positionGenerator()

    /*
     * EASY 레벨에서 컴퓨터가 결정한 위치를 반환 유저의 돌을 가장 적게 따는 위치들 중, 중심에 가장 가까운 위치를 결정한다.
     * 
     * 유저의 돌을 가장 적게 따는 위치들은 minPositionListGenerator() 가 반환한 리스트에 들어있고, 중심에 가장 가까운
     * 위치를 결정하기 위해 minDistanceList 와 minDistanceToBound() 를 사용한다. 중심에 가까운 정도를 판별하기
     * 위해 경계선까지의 최단거리를 이용하였다. (최단거리가 클수록 중심에 가깝다)
     */
    public Position positionGeneratorEasy() {
        int size;

        // 유저의 돌을 가장 적게 따는 위치들을 저장한 리스트
        ArrayList<Position> minPositionList = minPositionListGenerator();
        // minPositionList 의 모든 위치들에서의 경계선까지의 최단거리를 저장하는 리스트 (두 리스트의 크기는 같게 된다)
        ArrayList<Integer> minDistanceList = new ArrayList<Integer>();

        size = minPositionList.size();
        for (int i = 0; i < size; i++)
            // 경계선까지의 최단거리를 계산하여 리스트에 추가
            // 8방향 모두에서의 최단거리를 계산하기 때문에, 1 <= 최단거리 <= 4 가 된다.
            minDistanceList.add(minDistanceToBound(minPositionList.get(i)));

        // 경계선까지의 최단거리 중 최댓값(중심에 가장 가까운 돌을 판별하기 위해) 을 저장
        int maxDistance = Collections.max(minDistanceList);

        // 최종적으로, 유저의 돌을 가장 적게 따고, 경계선까지의 최단거리가 최대(중심에 가장 가까운)인 위치들을 저장하는 변수
        // 그런 위치가 여러개일 수 있기 때문에 리스트로 선언
        ArrayList<Position> maxDistancePositionList = new ArrayList<Position>();

        size = minPositionList.size(); // minDistanceList.size() 를 사용해도 동일하다.
        // 경계선까지의 최단거리가 최대(중심에 가장 가깝고)이고
        // 유저의 돌을 가장 적게 따는 위치들(minPositionList) 을 maxDistancePositionList 에 추가한다.
        for (int i = 0; i < size; i++)
            if (maxDistance == minDistanceList.get(i))
                maxDistancePositionList.add(minPositionList.get(i));

        // 유저의 돌을 가장 적게 따고, 중심에 가장 가까운 위치 여러개 중 하나를 랜덤으로 반환
        Position generatedPosition = maxDistancePositionList
                .get((int) (Math.random() * maxDistancePositionList.size()));

        return generatedPosition;
    } // positionGeneratorEasy()

    /*
     * NORMAL 레벨에서 컴퓨터가 결정한 위치를 반환
     * 
     * 컴퓨터가 둘 수 있는 위치들 중 하나를 랜덤으로 반환(HARD)하거나 유저의 돌을 가장 적게 따는 위치들 중 하나를 랜덤으로 반환
     * (EASY 와 다르게 중심에 가까운 정도는 고려하지 않는다)
     */
    public Position positionGeneratorNormal() {
        if ((int) (Math.random() * 2) == 0) {
            return positionGeneratorHard();
        } else {
            ArrayList<Position> minPositionList = minPositionListGenerator(); // 유저의 돌을 가장 적게 딸 수 있는 위치들의 리스트
            Position generatedPosition = minPositionList.get((int) (Math.random() * minPositionList.size()));

            return generatedPosition;
        }
    } // positionGeneratorNormal()

    /*
     * HARD 레벨에서 컴퓨터가 결정한 위치를 반환 컴퓨터가 둘 수 있는 위치들 중 하나를 랜덤으로 반환
     */
    public Position positionGeneratorHard() {
        return (currentTurn == Status.WHITE ? whiteAvailableList.get((int) (Math.random() * whiteAvailableList.size()))
                : blackAvailableList.get((int) (Math.random() * blackAvailableList.size())));
    } // positionGeneratorHard()

    /*
     * EXPERT 레벨에서 컴퓨터가 결정한 위치를 반환
     * 
     * 컴퓨터가 둘 수 있는 위치들 중 하나를 랜덤으로 반환(HARD)하거나 유저의 돌을 가장 많이 따는 위치들 중 하나를 랜덤으로 반환
     * (MASTER 와 다르게 경계선에 가까운 정도는 고려하지 않는다)
     */
    public Position positionGeneratorExpert() {
        if ((int) (Math.random() * 2) == 0) {
            return positionGeneratorHard();
        } else {
            ArrayList<Position> maxPositionList = maxPositionListGenerator(); // 유저의 돌을 가장 많이 딸 수 있는 위치들의 리스트
            Position generatedPosition = maxPositionList.get((int) (Math.random() * maxPositionList.size()));

            return generatedPosition;
        }
    } // positionGeneratorExpert()

    /*
     * MASTER 레벨에서 컴퓨터가 결정한 위치를 반환 유저의 돌을 가장 많이 따는 위치들 중, 경계선에 가장 가까운 위치를 결정한다.
     * (EASY 와 정반대라고 생각하면 된다)
     * 
     * 유저의 돌을 가장 많이 따는 위치들은 maxPositionListGenerator() 가 반환한 리스트에 들어있고, 경계선에 가장 가까운
     * 위치를 결정하기 위해 minDistanceList 와 minDistanceToBound() 를 사용한다. 경계선에 가까운 정도를 판별하기
     * 위해 경계선까지의 최단거리를 이용하였다. (최단거리가 작을수록 경계선에 가깝다)
     */
    public Position positionGeneratorMaster() {
        int size;

        // 유저의 돌을 가장 많이 따는 위치들을 저장한 리스트
        ArrayList<Position> maxPositionList = maxPositionListGenerator();
        // maxPositionList 의 모든 위치들에서의 경계선까지의 최단거리를 저장하는 리스트 (두 리스트의 크기는 같게 된다)
        ArrayList<Integer> minDistanceList = new ArrayList<Integer>();

        size = maxPositionList.size();
        for (int i = 0; i < size; i++)
            // 경계선까지의 최단거리를 계산하여 리스트에 추가
            // 8방향 모두에서의 최단거리를 계산하기 때문에, 1 <= 최단거리 <= 4 가 된다.
            minDistanceList.add(minDistanceToBound(maxPositionList.get(i)));

        // 경계선까지의 최단거리 중 최솟값(경계선에 가장 가까운 돌을 판별하기 위해) 을 저장
        int minDistance = Collections.min(minDistanceList);

        // 최종적으로, 유저의 돌을 가장 많이 따고, 경계선까지의 최단거리가 최소(경계선에 가장 가까운)인 위치들을 저장하는 변수
        // 그런 위치가 여러개일 수 있기 때문에 리스트로 선언
        ArrayList<Position> minDistancePositionList = new ArrayList<Position>();

        size = maxPositionList.size();
        // 경계선까지의 최단거리가 최소(경계선에 가장 가깝고)이고
        // 유저의 돌을 가장 많이 따는 위치들(maxPositionList) 을 minDistancePositionList 에 추가한다.
        for (int i = 0; i < size; i++)
            if (minDistance == minDistanceList.get(i))
                minDistancePositionList.add(maxPositionList.get(i));

        // 유저의 돌을 가장 많이 따고, 경계선에 가장 가까운 위치 여러개 중 하나를 랜덤으로 반환
        Position generatedPosition = minDistancePositionList
                .get((int) (Math.random() * minDistancePositionList.size()));

        return generatedPosition;
    } // positionGeneratorMaster()

    // 유저의 돌을 가장 적게 따는 위치들을 저장한 리스트를 생성하여 반환
    private ArrayList<Position> minPositionListGenerator() {
        int size;
        int currentRow;
        int currentCol;

        // 돌을 딸 수 있는지 여부를 판별하기 위해 먼저 가보기 위한 변수
        TraversingPosition traversal = new TraversingPosition();
        // traversal 를 이용해 돌을 딸 수 있다고 판별된 경우, 이동하며 딸 수 있는 돌의 개수를 세기 위한 변수
        TraversingPosition selectedPosition = new TraversingPosition();

        ArrayList<Position> availableList = (currentTurn == Status.WHITE ? whiteAvailableList : blackAvailableList);

        int point; // 각 위치에서 딸 수 있는 돌의 개수를 저장하기 위한 임시 변수
        // 각 위치(availableList)에서의 딸 수 있는 돌의 개수를 저장하는 리스트
        // pointList 와 availableList 의 크기는 같게 된다.
        ArrayList<Integer> pointList = new ArrayList<Integer>();

        size = availableList.size();
        for (int i = 0; i < size; i++) {
            point = 0;
            currentRow = availableList.get(i).getRow();
            currentCol = availableList.get(i).getCol();

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            // traversal 가 해당 (행, 열) 에서 상대의 돌인 동안 위로 이동한다. (사실상 컴퓨터가 실행하기 때문에 흑돌인 동안 이동)
            for (traversal.moveUp(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveUp())
                ;

            // 나의 색의 돌을 만나 멈춘 것이라면, for 문과 selectedPosition 을 이용해 해당 (행, 열) 과 traversal 사이에
            // 있는 상대의 돌의 개수를 셈
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveUp(); !selectedPosition.equals(traversal); selectedPosition.moveUp())
                    point++; // 위 방향으로 딸 수 있는 돌의 개수를 더함

            /*
             * 아래 코드들은 방향만 다를뿐 하는 작업은 동일하다.
             */

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveRightUp(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveRightUp())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveRightUp(); !selectedPosition.equals(traversal); selectedPosition
                        .moveRightUp())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveRight(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveRight())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveRight(); !selectedPosition.equals(traversal); selectedPosition.moveRight())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveRightDown(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal
                            .moveRightDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveRightDown(); !selectedPosition.equals(traversal); selectedPosition
                        .moveRightDown())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveDown(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveDown(); !selectedPosition.equals(traversal); selectedPosition.moveDown())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveLeftDown(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveLeftDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveLeftDown(); !selectedPosition.equals(traversal); selectedPosition
                        .moveLeftDown())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveLeft(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveLeft())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveLeft(); !selectedPosition.equals(traversal); selectedPosition.moveLeft())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveLeftUp(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveLeftUp())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveLeftUp(); !selectedPosition.equals(traversal); selectedPosition.moveLeftUp())
                    point++;

            // 해당 (행, 열) 에서 상대의 돌을 딸 수 있는 개수를 리스트에 추가
            pointList.add(point);
        }

        // 상대의 돌을 가장 적게 따는 개수를 저장
        int min = Collections.min(pointList);

        // 상대의 돌을 가장 적게 따는 위치들을 저장하기 위한 리스트
        ArrayList<Position> minPositionList = new ArrayList<Position>();

        size = availableList.size();
        for (int i = 0; i < size; i++)
            if (pointList.get(i) == min) // 해당 위치에서 상대의 돌을 가장 적게 따는 경우
                minPositionList.add(availableList.get(i)); // minPositionList 에 위치 추가

        return minPositionList; // 상대의 돌을 가장 적게 따는 위치들을 반환
    } // minPositionListGenerator()

    // 유저의 돌을 가장 많이 따는 위치들을 저장한 리스트를 생성하여 반환
    private ArrayList<Position> maxPositionListGenerator() {
        int size;
        int currentRow;
        int currentCol;

        // 돌을 딸 수 있는지 여부를 판별하기 위해 먼저 가보기 위한 변수
        TraversingPosition traversal = new TraversingPosition();
        // traversal 를 이용해 돌을 딸 수 있다고 판별된 경우, 이동하며 딸 수 있는 돌의 개수를 세기 위한 변수
        TraversingPosition selectedPosition = new TraversingPosition();

        ArrayList<Position> availableList = (currentTurn == Status.WHITE ? whiteAvailableList : blackAvailableList);

        int point; // 각 위치에서 딸 수 있는 돌의 개수를 저장하기 위한 임시 변수
        // 각 위치(availableList)에서의 딸 수 있는 돌의 개수를 저장하는 리스트
        // pointList 와 availableList 의 크기는 같게 된다.
        ArrayList<Integer> pointList = new ArrayList<Integer>();

        size = availableList.size();
        for (int i = 0; i < size; i++) {
            point = 0;
            currentRow = availableList.get(i).getRow();
            currentCol = availableList.get(i).getCol();

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            // traversal 가 해당 (행, 열) 에서 상대의 돌인 동안 위로 이동한다. (사실상 컴퓨터가 실행하기 때문에 흑돌인 동안 이동)
            for (traversal.moveUp(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveUp())
                ;

            // 나의 색의 돌을 만나 멈춘 것이라면, for 문과 selectedPosition 을 이용해 해당 (행, 열) 과 traversal 사이에
            // 있는 상대의 돌의 개수를 셈
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveUp(); !selectedPosition.equals(traversal); selectedPosition.moveUp())
                    point++; // 위 방향으로 딸 수 있는 돌의 개수를 더함

            /*
             * 아래 코드들은 방향만 다를뿐 하는 작업은 동일하다.
             */

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveRightUp(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveRightUp())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveRightUp(); !selectedPosition.equals(traversal); selectedPosition
                        .moveRightUp())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveRight(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveRight())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveRight(); !selectedPosition.equals(traversal); selectedPosition.moveRight())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveRightDown(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal
                            .moveRightDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveRightDown(); !selectedPosition.equals(traversal); selectedPosition
                        .moveRightDown())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveDown(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveDown(); !selectedPosition.equals(traversal); selectedPosition.moveDown())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveLeftDown(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveLeftDown())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveLeftDown(); !selectedPosition.equals(traversal); selectedPosition
                        .moveLeftDown())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveLeft(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveLeft())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveLeft(); !selectedPosition.equals(traversal); selectedPosition.moveLeft())
                    point++;

            traversal.setPosition(currentRow, currentCol);
            selectedPosition.setPosition(currentRow, currentCol);

            for (traversal.moveLeftUp(); status[traversal.getRow()][traversal
                    .getCol()] == (currentTurn == Status.WHITE ? Status.BLACK : Status.WHITE); traversal.moveLeftUp())
                ;
            if (status[traversal.getRow()][traversal.getCol()] == (currentTurn == Status.WHITE ? Status.WHITE
                    : Status.BLACK))
                for (selectedPosition.moveLeftUp(); !selectedPosition.equals(traversal); selectedPosition.moveLeftUp())
                    point++;

            // 해당 (행, 열) 에서 상대의 돌을 딸 수 있는 개수를 리스트에 추가
            pointList.add(point);
        }

        // 상대의 돌을 가장 많이 따는 개수를 저장
        int max = Collections.max(pointList);

        // 상대의 돌을 가장 많이 따는 위치들을 저장하기 위한 리스트
        ArrayList<Position> maxPositionList = new ArrayList<Position>();

        size = availableList.size();
        for (int i = 0; i < size; i++)
            if (pointList.get(i) == max) // 해당 위치에서 상대의 돌을 가장 많이 따는 경우
                maxPositionList.add(availableList.get(i)); // maxPositionList 에 위치 추가

        return maxPositionList; // 상대의 돌을 가장 많이 따는 위치들을 반환
    } // maxPositionListGenerator()

    // 해당 (행, 열) 에서 8방향으로의 경계선까지의 최단거리를 반환
    private int minDistanceToBound(Position rPosition) {
        int currentRow = rPosition.getRow();
        int currentCol = rPosition.getCol();

        // 8방향으로 이동시켜 보기 위한 변수
        TraversingPosition traversal = new TraversingPosition();

        int distance; // 한 방향으로 이동시켰을때 경계선까지의 거리를 저장하는 변수
        int minDistance; // 현재 (행, 열) 에서 경계선까지의 최단거리를 저장

        distance = 1;
        traversal.setPosition(currentRow, currentCol);
        // 경계선을 만날때까지 distance 를 증가시키며 위로 이동
        for (traversal.moveUp(); status[traversal.getRow()][traversal.getCol()] != Status.BOUND; traversal
                .moveUp(), distance++)
            ;
        minDistance = distance; // minDistance 초기화

        /*
         * 아래 코드들은 방향만 다를뿐 하는 작업은 동일하다.
         */

        distance = 1;
        traversal.setPosition(currentRow, currentCol);
        for (traversal.moveRightUp(); status[traversal.getRow()][traversal.getCol()] != Status.BOUND; traversal
                .moveRightUp(), distance++)
            ;
        if (distance < minDistance) {
            minDistance = distance;
        } // minDistance 갱신

        distance = 1;
        traversal.setPosition(currentRow, currentCol);
        for (traversal.moveRight(); status[traversal.getRow()][traversal.getCol()] != Status.BOUND; traversal
                .moveRight(), distance++)
            ;
        if (distance < minDistance) {
            minDistance = distance;
        }

        distance = 1;
        traversal.setPosition(currentRow, currentCol);
        for (traversal.moveRightDown(); status[traversal.getRow()][traversal.getCol()] != Status.BOUND; traversal
                .moveRightDown(), distance++)
            ;
        if (distance < minDistance) {
            minDistance = distance;
        }

        distance = 1;
        traversal.setPosition(currentRow, currentCol);
        for (traversal.moveDown(); status[traversal.getRow()][traversal.getCol()] != Status.BOUND; traversal
                .moveDown(), distance++)
            ;
        if (distance < minDistance) {
            minDistance = distance;
        }

        distance = 1;
        traversal.setPosition(currentRow, currentCol);
        for (traversal.moveLeftDown(); status[traversal.getRow()][traversal.getCol()] != Status.BOUND; traversal
                .moveLeftDown(), distance++)
            ;
        if (distance < minDistance) {
            minDistance = distance;
        }

        distance = 1;
        traversal.setPosition(currentRow, currentCol);
        for (traversal.moveLeft(); status[traversal.getRow()][traversal.getCol()] != Status.BOUND; traversal
                .moveLeft(), distance++)
            ;
        if (distance < minDistance) {
            minDistance = distance;
        }

        distance = 1;
        traversal.setPosition(currentRow, currentCol);
        for (traversal.moveLeftUp(); status[traversal.getRow()][traversal.getCol()] != Status.BOUND; traversal
                .moveLeftUp(), distance++)
            ;
        if (distance < minDistance) {
            minDistance = distance;
        }

        return minDistance; // 8방향에서의 경계선까지의 최단거리 반환
    } // minDistanceToBound()

    // 리스트에 삽입할때, 중복을 줄이기 위해 동일한 위치가 있는지 확인후 추가
    private void pushElement(ArrayList<Position> list, Position element) {
        int size = list.size();
        for (int i = 0; i < size; i++)
            if (list.get(i).equals(element))
                return;

        list.add(new Position(element.getRow(), element.getCol()));
    } // pushElement()
} // Reversi Class
