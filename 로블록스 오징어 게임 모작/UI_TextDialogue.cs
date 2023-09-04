using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using DG.Tweening;

// 하나의 대화 상자를 띄우는 데에 필요한 정보들
class ConversationEntity
{
    public bool isEnd = false;
    public bool isPlaying = false;
    public string text;
    public float wordTerm;          // 한 글자씩 표시되는 시간 간격
    public float endDelay;
    public int fontSize;
    public AudioClip audioClip;
}

// 차례로 입력한 대화들을 큐에 넣고 하나씩 띄워주는 유틸리티 클래스다.
// 싱글톤 패턴으로 구현해 어디서든지 쉽게 사용할 수 있다.
public class UI_TextDialogue : MonoBehaviour
{
    public static UI_TextDialogue Instance;

    void Awake()
    {
        if (Instance == null)
            Instance = this;
        else
            Destroy(this);
    }

    // 대화 상자 띄우기를 시작할 때 발동하는 델리게이트
    // (게임 상태 변경 등)
    [HideInInspector]
    public Action onStart;
    // 대화 상자가 끝났을 때 발동하는 델리게이트
    [HideInInspector]
    public DG.Tweening.TweenCallback onComplete;

    // UI
    RectTransform dialoguePanel;
    Text conversationText;

    // 입력된 대화들을 순차적으로 큐에 넣어 관리한다.
    Queue<ConversationEntity> conversationQueue;
    // 대화 상자가 나타나는 중인지 저장
    bool isAppearingDialogue;

    void Start()
    {
        dialoguePanel = transform.Find("Bottom Center Alignment").GetComponent<RectTransform>();
        conversationText = dialoguePanel.Find("Conversation Text").GetComponent<Text>();

        dialoguePanel.anchoredPosition = Vector2.down * 300;

        conversationQueue = new Queue<ConversationEntity>();
    }

    void Update()
    {
        ConversationEntity convEntity = null;

        // 대화 상자가 완전히 나타났으면
        if (!isAppearingDialogue)
            // 큐에서 대화를 1개 Peek한다.
            if (conversationQueue.TryPeek(out convEntity))
                // 대화가 끝났으면 Pop한다.
                if (convEntity.isEnd)
                    conversationQueue.Dequeue();
                // Peek한 대화가 재생 중이 아니면, 재생한다.
                else if (!convEntity.isPlaying)
                    PlayConversationText(convEntity);
    }

    // 입력된 대화를 큐에 삽입한다.
    public void EnqueueConversationText(string text = "", float wordTerm = 0.1f, float endDelay = 1f, int fontSize = 36, AudioClip audioClip = null)
    {
        ConversationEntity convEntity = new ConversationEntity();

        convEntity.text = text;
        convEntity.wordTerm = wordTerm;
        convEntity.endDelay = endDelay;
        convEntity.fontSize = fontSize;
        convEntity.audioClip = audioClip;

        conversationQueue.Enqueue(convEntity);
    }

    void PlayConversationText(ConversationEntity convEntity)
    {
        StartCoroutine(IEPlayConversationText(convEntity));
    }

    // 코루틴을 이용해 wordTerm마다 한 문자씩(공백 제외) 대화를 출력한다.
    IEnumerator IEPlayConversationText(ConversationEntity convEntity)
    {
        convEntity.isEnd = false;
        convEntity.isPlaying = true;

        conversationText.text = "";
        conversationText.fontSize = convEntity.fontSize;

        if (convEntity.audioClip)
            AudioManager.Instance.PlayOneShot(convEntity.audioClip);

        foreach (char ch in convEntity.text)
        {
            if (ch != ' ')
                yield return new WaitForSeconds(convEntity.wordTerm);

            conversationText.text += ch;
        }

        yield return new WaitForSeconds(convEntity.endDelay);

        convEntity.isEnd = true;
        convEntity.isPlaying = false;
    }

    public void AppearTextDialogue(float destY = 150f, float duration = 1.5f)
    {
        // 대화 상자를 띄울 때 델리게이트를 발동한다.
        onStart();

        conversationText.text = "";

        isAppearingDialogue = true;
        
        dialoguePanel.DOAnchorPosY(destY, duration).SetEase(Ease.OutBounce).OnComplete(() => { isAppearingDialogue = false; });
    }

    public void DisappearTextDialogue(float destY = -300f, float duration = 1.5f)
    {
        StartCoroutine(IEDisappearTextDialogue(destY, duration));
    }

    IEnumerator IEDisappearTextDialogue(float destY, float duration)
    {
        // 모든 대화가 끝날 때까지 대기한다.
        while (conversationQueue.Count != 0)
            yield return null;

        // 대화 상자를 내리고 종료 델리게이트를 발동한다.
        dialoguePanel.DOAnchorPosY(destY, duration).SetEase(Ease.InBounce).OnComplete(onComplete);
    }
}
