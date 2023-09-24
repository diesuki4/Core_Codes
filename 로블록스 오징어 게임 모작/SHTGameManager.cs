using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using Photon.Pun;
using Photon.Realtime;

public class SHTGameManager : MonoBehaviourPunCallbacks
{
    public static SHTGameManager Instance;

    void Awake()
    {
        if (Instance == null)
            Instance = this;
        else
            Destroy(this);
    }

    enum State
    {
        Idle = 1,
        Conversation = 2,
        Initialize = 4,
        InGame = 8,
        Result = 16,
        Alive = 32,
        Die = 64,
        End = 128,
        allEnd = 256,
    }
    State state;

    [Header("달고나 게임 시간")]
    public float inGameTime;

    public GameObject goPlayer;
    public Player playerComp;
    // 들어온 순서대로 배정될 플레이어, 에이전트 위치
    public Transform spawnPositions;
    public Transform spawnPositionsAgent;

    float currentTime;

    Animator agentAnim;
    bool isGameFinished;

    int playerCount;            // 현재 입장한 플레이어 수
    private int playerEndCount; // 게임이 끝난 플레이어 수
    private bool isEnd;
    float uniqueValue;  // 방장에게 자신의 위치를 질의하기 위한 고유값

    void Start()
    {
        goPlayer = PhotonNetwork.Instantiate("Player", Vector3.zero, Quaternion.identity);
        playerComp = goPlayer.GetComponent<Player>();

        GameObject agent = PhotonNetwork.Instantiate("Agent", Vector3.zero, Quaternion.identity);
        agentAnim = agent.GetComponent<Animator>();

        // 클라이언트는 레벨에 입장 시, 생성한 고유값을 방장에게 보내 자신의 위치를 질의한다.
        uniqueValue = Random.Range(float.MinValue, float.MaxValue);
        
        photonView.RPC("RpcRequestSetPlayerPosition", RpcTarget.MasterClient, uniqueValue);

        state = State.Idle;
    }

    // 고유값을 받은 방장은, 모두에게 방금 입장한 사람이 위치할 인덱스와 고유값을 브로드 캐스트한다.
    [PunRPC]
    void RpcRequestSetPlayerPosition(float uniqueValue)
    {
        photonView.RPC("RpcSetPlayerPosition", RpcTarget.All, playerCount, uniqueValue);
        photonView.RPC("RpcSetPlayerCount", RpcTarget.Others, ++playerCount);
    }

    // 브로드 캐스트를 받은 클라이언트 중, 고유값이 일치하는(질의를 보냈던) 클라이언트가
    // 방장이 알려준 위치에서 시작하게 된다.
    [PunRPC]
    void RpcSetPlayerPosition(int posIdx, float uniqueValue)
    {
        if (Mathf.Approximately(this.uniqueValue, uniqueValue))
        {
            // 클라이언트 별로 에이전트의 위치는 다르다.
            // 플레이어 앞에 위치하기 때문이다.
            goPlayer.transform.position = spawnPositions.GetChild(posIdx).position;
            goPlayer.transform.rotation = spawnPositions.GetChild(posIdx).rotation;

            agentAnim.transform.position = spawnPositionsAgent.GetChild(posIdx).position;
            agentAnim.transform.rotation = spawnPositionsAgent.GetChild(posIdx).rotation;
        }
    }

    [PunRPC]
    void RpcSetPlayerCount(int playerCount)
    {
        this.playerCount = playerCount;
    }

    void Update()
    {
        switch (state)
        {
            case State.Idle :
                UpdateIdle();
                break;
            case State.Conversation :
                break;
            case State.Initialize :
                UpdateInitialize();
                break;
            case State.InGame :
                UpdateInGame();
                break;
            case State.Result :
                UpdateResult();
                break;
            case State.Alive :
                UpdateAlive();
                break;
            case State.Die :
                UpdateDie();
                break;
            case State.End :
                UpdateEnd();
                break;
            case State.allEnd :
                break;
        }

        EndDetect();
    }

    // 방장이 모든 플레이어의 게임이 종료되었는지 확인하는 함수다.
    private void EndDetect()
    {
        // 방장이면
        if (PhotonNetwork.IsMasterClient && isEnd == false)
        {
            // 현재 클라이언트 수와 종료 플레이어 수가 같은지 확인한다.
            if (playerEndCount == PhotonNetwork.CurrentRoom.PlayerCount)
            {
                // 내가 죽었으면, 나가고 다음 사람에게 방장을 넘긴다.
                // (playerEndCount는 RPC 함수를 통해 모든 클라이언트에서 증가되었다.)
                if (playerComp.state == Player.State.Die)
                {
                    PhotonNetwork.LeaveRoom();
                    PhotonNetwork.LeaveLobby();
                    PhotonNetwork.Disconnect();
                    Application.Quit();
                }
                // 내가 살았으면, 클라이언트들과 다음 레벨로 이동한다.
                else
                {
                    PhotonNetwork.LoadLevel("MarbleGameScene");
                }

                isEnd = true;
            }
        }
    }

    void UpdateIdle()
    {
        SHTGameUIManager.Instance.ShowAllUI(false);

        // 델리게이트에 람다 함수를 등록하고, 대화 상자를 하나씩 띄운다.
        UI_TextDialogue.Instance.onStart = () => { state = State.Conversation; };
        UI_TextDialogue.Instance.AppearTextDialogue();
        UI_TextDialogue.Instance.EnqueueConversationText("달고나 게임에 오신 것을 환영합니다!");
        UI_TextDialogue.Instance.EnqueueConversationText("선을 따라 달고나를 자르세요!");
        UI_TextDialogue.Instance.EnqueueConversationText("달고나가 깨지면 탈락하게 됩니다.");
        UI_TextDialogue.Instance.DisappearTextDialogue();
        UI_TextDialogue.Instance.onComplete = () => { state = State.Initialize; };
    }

    void UpdateInitialize()
    {
        SHTGameUIManager.Instance.ShowAllUI(true);
        SHTGameUIManager.Instance.ShowAllDrawArea(false);

        state = State.InGame;
    }

    void UpdateInGame()
    {
        currentTime += Time.deltaTime;

        // 게임 시간 동안
        if (currentTime < inGameTime)
        {
            // 타이머 시간을 갱신한다.
            SHTGameUIManager.Instance.SetCountDownText(inGameTime - currentTime);

            if (Input.GetMouseButton(0))
            {
                // 마우스 위치마다 원을 여러 개 찍어 선을 만든다.
                if (Input.GetAxisRaw("Mouse X") != 0 || Input.GetAxisRaw("Mouse Y") != 0)
                    SHTGameUIManager.Instance.DrawCircle(Input.mousePosition);
                    
                Image hitImage = SHTGameUIManager.Instance.GraphicRaycast(Input.mousePosition);

                // 마우스에 닿은 UI 객체가
                if (hitImage)
                    // 안쪽 체크포인트면 지났다고 체크
                    if (SHTGameUIManager.Instance.IsInnerArea(hitImage))
                        SHTGameUIManager.Instance.ShowDrawArea(hitImage, true);
                    // 달고나에 닿았으면 실패
                    else
                        state = State.Result;

                // 모든 체크포인트를 지났으면 성공
                if (SHTGameUIManager.Instance.IsAllDrawAreaVisible())
                    if (!isGameFinished)
                        StartCoroutine(SetStateAfter(State.Result, 0.5f));
            }
        }
        else
        {
            state = State.Result;
        }
    }

    IEnumerator SetStateAfter(State state, float delay)
    {
        isGameFinished = true;

        yield return new WaitForSeconds(delay);

        this.state = state;
    }

    // 결과 화면
    void UpdateResult()
    {
        // 모든 체크포인트를 지났으면 성공
        bool result = SHTGameUIManager.Instance.IsAllDrawAreaVisible();

        SHTGameUIManager.Instance.ShowAllUI(false);

        // 델리게이트에 람다 함수를 등록하고, 대화 상자를 하나씩 띄운다.
        UI_TextDialogue.Instance.onStart = () => { state = State.Conversation; };
        UI_TextDialogue.Instance.AppearTextDialogue();
        UI_TextDialogue.Instance.EnqueueConversationText("게임이 종료되었습니다.");
        UI_TextDialogue.Instance.EnqueueConversationText("달고나가 잘 뽑" + (result ? "혔습니다. " : "히지 않았습니다."));
        if (!result) UI_TextDialogue.Instance.EnqueueConversationText("아쉽군요.");
        UI_TextDialogue.Instance.DisappearTextDialogue();
        UI_TextDialogue.Instance.onComplete = () => { state = (result) ? State.Alive : State.Die; };
    }

    void UpdateAlive()
    {
        if (GameManager.Instance.debugMode)
            Debug.Log("[SHTGameManager] 살아남았습니다!!");

        state = State.End;
    }

    // 죽었으면 네트워크 연결을 종료한다.
    void UpdateDie()
    {
        agentAnim.SetTrigger("Load");

        playerComp.Die(Player.DieType.FlyAway);

        DisconnectPhoton();
    }

    // 게임 종료
    void UpdateEnd()
    {
        // 자신의 플레이가 종료되면 모든 클라이언트에게
        // 종료 플레이어 수를 증가시키라고 요청한다.
        // (방장이 바뀔 경우 우선 순위에 따라 다음 사람이 방장이 되기 때문이다.)
        photonView.RPC("AddPlayerEndCount", RpcTarget.All);
        
        state = State.allEnd;
    }

    // 중료된 플레이어 수를 증가시킨다.
    [PunRPC]
    void AddPlayerEndCount()
    {
        playerEndCount++;
    }

    // 네트워크 연결 종료
    void DisconnectPhoton()
    {
        PhotonNetwork.Destroy(goPlayer);
        PhotonNetwork.LeaveRoom();
        PhotonNetwork.LeaveLobby();
        PhotonNetwork.Disconnect();

        state = State.allEnd;
    }
}
