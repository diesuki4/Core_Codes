using System;
using System.Collections;
using System.Collections.Generic;
using Cysharp.Threading.Tasks;
using UniRx;
using UnityEngine;

// 인게임에서 가상 고양이와 놀아줄 때 사용되는 게임 매니저다.
// 일정 시간 마다 호감도, 굶주림, 청결도를 감소시키며
// 서버에 게임 데이터를 업로드한다.
public class MainGameManager : MonoBehaviour
{
    public static MainGameManager Instance;

    public int deviceId;    // 기기 고유 식별 번호 (유저 ID 대체)
    public int affection;   // 호감도
    public int starvation;  // 굶주림
    public int cleanliness; // 청결도

    // 게임 데이터가 변할 때 발동하는 델리게이트
    // UI 갱신 함수 등이 등록된다.
    public Action onChangeCallback;

    void Awake()
    {
        if (Instance == null)
            Instance = this;
        else
            Destroy(gameObject);
    }

    void Start()
    {
        // 기기 고유 식별 번호 저장
        deviceId = SystemInfo.deviceUniqueIdentifier;

        // 1초마다 호감도를 감소시킨다.
        StartCoroutine(IEDecreaseAffectionEverySec(1f));
        // 2초마다 굶주림과 청결도를 감소시킨다.
        StartCoroutine(IEDecreaseStarvCleanEverySec(2f));
        // 1분마다 현재 데이터를 서버에 저장한다.
        StartCoroutine(IESaveEveryMinute(1f));
    }

    // 설정한 초마다 호감도를 감소시킨다.
    IEnumerator IEDecreaseAffectionEverySec(float sec)
    {
        while (true)
        {
            yield return new WaitForSeconds(sec);

            if (UIManager.Instance.GameState == GameState.Main)
            {
                affection -= 1;

                // 델리게이트에는 UI 갱신 함수가 등록되어 있다.
                onChangeCallback?.Invoke();
            }
        }
    }
    
    // 설정한 초마다 굶주림과 청결도를 감소시킨다.
    IEnumerator IEDecreaseStarvCleanEverySec(float sec)
    {
        while (true)
        {
            yield return new WaitForSeconds(sec);

            if (UIManager.Instance.GameState == GameState.Main)
            {
                starvation -= 1;
                cleanliness -= 1;

                // 델리게이트에는 UI 갱신 함수가 등록되어 있다.
                onChangeCallback?.Invoke();
            }
        }
    }

    // 설정한 분마다 현재 데이터를 서버에 저장한다.
    IEnumerator IESaveEveryMinute(float minute)
    {
        while (true)
        {
            yield return new WaitForSeconds(minute);

            if (UIManager.Instance.GameState == GameState.Main)
            {
                Save();

                // 델리게이트에는 UI 갱신 함수가 등록되어 있다.
                onChangeCallback?.Invoke();
            }
        }
    }

    // 기기 오규 식별 번호, 호감도, 굶주림, 청결도를
    // 비동기 방식으로 POST 요청을 보내 서버에 저장한다.
    async UniTaskVoid Save()
    {
        RequestSavePacket packet = new RequestSavePacket();

        packet.deviceId = deviceId;
        packet.affection = affection;
        packet.starvation = starvation;
        packet.cleanliness = cleanliness;

        var response = await NetManager.Post<ResponseSavePacket>(packet);
        Debug.LogError(response.result);
    }
}
