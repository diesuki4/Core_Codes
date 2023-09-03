using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 오디오 매니저
public class AudioManager : MonoBehaviour
{
    public static AudioManager Instance;

    void Awake()
    {
        if (Instance == null)
        {
            Instance = this;
            
            audios = GetComponents<AudioSource>();
        }
    }

    public enum AudioType
    {
        VillageRemix,   // 배찌 뒹글뒹글 - 빌리지 리믹스
        CountDown,      // 카운트다운
        Engine,         // 엔진
        Drift,          // 드리프트
        NormalBooster,  // 일반 부스터
        TouchBooster,   // 터치 부스터
        ShortBooster,   // 짧은 부스터
        FinishLinePass, // 결승선 통과
        RaceFailed,     // 완주 실패
        Backward,       // 뒤로갈 때
        LapChange,      // 랩 바뀔 때
        Timer           // 타이머 시간 부족 때
    }

    public float enginePitchMultiflier = 1.2f;

    AudioSource[] audios;
    PlayerDrive PD;
    Boost boost;
    float maxSpeed;

    void Start()
    {
        // AI와 대전하는 싱글 플레이 게임이므로, 편의를 위해 플레이어를 싱글톤으로 구현
        PD    = Player.Instance.GetComponent<PlayerDrive>();
        boost = Player.Instance.GetComponent<Boost>();

        maxSpeed = Mathf.Max(new float[]{boost.turboBoostMaxSpeed, boost.touchBoostMaxSpeed,
                                         boost.mmtBoostMaxSpeed, boost.normalBoostMaxSpeed});
    }

    void Update()
    {
        EnginePitchController();
    }

    public void PlayAudio(AudioType audioType, bool bStop = false)
    {
        if (bStop)
            audios[(int)audioType].Stop();
        else
            audios[(int)audioType].Play();
    }

    public void StopAllAudio()
    {
        foreach (AudioSource audio in audios)
            audio.Stop();
    }

    void EnginePitchController()
    {
        audios[(int)AudioType.Engine].pitch = PD.currentSpeed / maxSpeed * enginePitchMultiflier;
    }
}
