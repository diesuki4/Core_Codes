using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 부스터 담당 컴포넌트
public class Boost : MonoBehaviour
{
    // 일반 부스터 가동 시간, 최대 속력
    public float normalBoostTime, normalBoostMaxSpeed;
    // 총 일반 부스터 게이지
    public float normalBoostTotalGauge;
    // 현재 일반 부스터 게이지
    public float normalBoostGauge;
    // 일반 부스터 게이지 증가량 (deltaTime 당)
    public float normalBoostGaugeIncreaseSpeed;
    // 드리프트 시 일반 부스터 게이지 증가량 (deltaTime 당)
    public float normalBoostDriftGaugeIncreaseSpeed;
    // 일반 부스터 사용 가능 여부
    public bool bNormalBoostAvailable;
    // 최대 부스터 충전 개수 (2개 고정)
    int normalBoostMaxCount = 2;
    // 현재 일반 부스터 소지 개수
    int normalBoostCount;

    // 순간 부스터 가동 시간, 최대 속력
    public float mmtBoostTime, mmtBoostMaxSpeed;
    // 순간 부스터 사용을 위한 최소 드리프트 시간
    public float mmtBoostRequireTime;
    // 순간 부스터 활성화 시 사용 가능 시간
    public float mmtBoostAvailableTime;
    // 순간 부스터 사용 가능 여부
    public bool bMMTBoostAvailable;

    // 터치 부스터 가동 시간, 최대 속력
    public float touchBoostTime, touchBoostMaxSpeed;
    // 터치 부스터 게이지
    public int touchBoostGauge;
    // UI 에 터치 부스터가 보이기 위한 최소 터치 횟수
    public int touchBoostGaugeUIMinCount;
    // 터치 부스터 발동까지 필요한 총 터치 횟수
    public int touchBoostTotalGauge;
    // 터치 부스터 사용 가능 여부
    public bool bTouchBoostAvailable;
    // 터치 부스터 게이지 누적 가능 시간
    public float touchBoostKeepAvailableTime;
    // touchBoostKeepAvailableTime 동안 터치 없을 시
    // 터치 부스터 게이지 초기화를 위해 시간을 누적하는 변수 
    float touchBoostKeepCurrentTime;

    // 터보(시작) 부스터 가동 시간, 최대 속력
    public float turboBoostTime, turboBoostMaxSpeed;
    // 터보(시작) 부스터 사용 가능 시간
    public float turboBoostAvailableTime;

    // 부스터 박스 접촉 시 부스터 가동 시간, 최대 속력
    public float boostBoxTime, boostBoxMaxSpeed;

    // 부스터 사용 시 기존 대비 속력이 증가하는 배수 (deltaTime 당)
    public float boostSpeedFactor;
    // 부스터 사용 시 최대 속력이 증가하는 배수 (deltaTime 당)
    public float boostMaxSpeedFactor;
    // 부스터 종료 후 복구에 걸리는 시간
    public float recoverTime;
    // 총 부스터 사용 횟수 누적
    public float totalBoostCount;
    // 부스터 사용 시 효과
    public GameObject postProcessVolume;

    // 플레이어 주행 컴포넌트 캐시
    PlayerDrive PD;
    // boostSpeedFactor 초깃값을 백업하는 변수
    float o_boostSpeedFactor;

    void Start()
    {
        PD = GetComponent<PlayerDrive>();

        // boostSpeedFactor 초깃값 백업
        o_boostSpeedFactor = boostSpeedFactor;
        // 기본 주행 시에는 1로 설정한다.
        boostSpeedFactor = 1.f;

        normalBoostGauge = 0;

        bTouchBoostAvailable  = true;
        bNormalBoostAvailable = true;
    }

    void Update()
    {
        NormalBoostGaugeController();
        TouchBoostController();
        BoostConcurrencyController();
    }

    // 부스터 발동 후 성공 여부 반환
    public bool Boost(Player.State state)
    {
        float boostTime = 0, maxSpeed = 0;

        // 이미 부스터를 사용 중이면 발동 불가
        if ((state & (Player.State.NormalBoost | Player.State.MMTBoost |
            Player.State.TouchBoost | Player.State.TurboBoost |
            Player.State.BoostBox)) == 0)
            return false;
        else
            StopCoroutine("IEBoost");

        if (Player.Instance.state != Player.State.Drift)
            Player.Instance.state = state;

        // 백업했던 부스터 속도 증가 초기 설정값을 가져온다.
        boostSpeedFactor = o_boostSpeedFactor;

        switch (state)
        {
        case Player.State.NormalBoost : // 일반 부스터
            boostTime = normalBoostTime;
            maxSpeed  = normalBoostMaxSpeed;
            break;
        case Player.State.MMTBoost :    // 순간(Momentary) 부스터
            boostTime = mmtBoostTime;
            maxSpeed  = mmtBoostMaxSpeed;
            break;
        case Player.State.TouchBoost :  // 터치 부스터
            boostTime = touchBoostTime;
            maxSpeed  = touchBoostMaxSpeed;
            break;
        case Player.State.TurboBoost :  // 터보(시작) 부스터
            boostTime = turboBoostTime;
            maxSpeed  = turboBoostMaxSpeed;
            break;
        case Player.State.BoostBox :    // 부스터 상자
            boostTime = boostBoxTime;
            maxSpeed  = boostBoxMaxSpeed;
            break;
        }

        StartCoroutine("IEBoost", new object[]{boostTime, maxSpeed});

        return true;
    }

    // 부스터 종류에 따라 지속 시간, 최대 속력를 인자로 받는다.
    IEnumerator IEBoost(object[] args)
    {
        float boostTime = (float)args[0];
        float maxSpeed  = (float)args[1];

        postProcessVolume.SetActive(true);

        PlayerDrive PD = GetComponent<PlayerDrive>();
        
        float t = 0;

        // 지속 시간 동안 최대 속력을 boostMaxSpeedFactor 속도로 maxSpeed까지 증가시킨다.
        while (t < boostTime)
        {
            t += Time.deltaTime;

            PD.maxSpeed = Mathf.Clamp(PD.maxSpeed * boostMaxSpeedFactor, PD.o_maxSpeed, maxSpeed);

            yield return null;
        }

        t = 0;

        // PD.o_maxSpeed = 플레이어 기본 최대 속력
        // 복구 시간 동안 최대 속력을 boostMaxSpeedFactor 속도로 PD.o_maxSpeed까지 감소시킨다.
        while (t < recoverTime)
        {
            t += Time.deltaTime;

            PD.maxSpeed = Mathf.Clamp(PD.maxSpeed / boostMaxSpeedFactor, PD.o_maxSpeed, maxSpeed);

            yield return null;
        }

        // 드리프트 중, 점프대 사용중, 경주 종료가 아니면 주행 상태로 변경
        if ((Player.Instance.state & (Player.State.Drift | Player.State.JumpBox | Player.State.End)) == 0)
            Player.Instance.state = Player.State.Drive;

        // 주행 속력 복구
        PD.maxSpeed = PD.o_maxSpeed;
        boostSpeedFactor = 1.f;

        bTouchBoostAvailable  = true;
        bNormalBoostAvailable = true;

        postProcessVolume.SetActive(false);
    }

    // 순간(Momentary) 부스터 활성화 함수
    // (드리프트 이후 잠깐 동안 사용할 수 있다.)
    public void EnableMMTBoost()
    {
        bMMTBoostAvailable = true;

        UIManager.Instance.MMTBoostButtonSetActive(true);

        StartCoroutine(IEEnableMMTBoost());
    }

    // 순간 부스터는 드리프트 직후, 일정 시간 내에 사용하지 않으면 소멸
    IEnumerator IEEnableMMTBoost()
    {
        float t = 0;

        while (bMMTBoostAvailable & (t += Time.deltaTime) < mmtBoostAvailableTime)
            yield return null;

        bMMTBoostAvailable = false;
        UIManager.Instance.MMTBoostButtonSetActive(false);
    }

    // 순간 부스터 발동 래퍼 함수
    public void MMTBoostExecute()
    {
        if (!bMMTBoostAvailable)
            return;

        Boost(Player.State.MMTBoost);
        AudioManager.Instance.PlayAudio(AudioManager.AudioType.ShortBooster);

        bMMTBoostAvailable = false;
    }

    // 일반 부스터 게이지 컨트롤러
    // (일반 주행을 통해 누적할 수 있고, 드리프트 시에 더 많이 누적된다.)
    void NormalBoostGaugeController()
    {
        if (Player.Instance.state == Player.State.Drive)
            normalBoostGauge += normalBoostGaugeIncreaseSpeed * Time.deltaTime;
        else if (Player.Instance.state == Player.State.Drift)
            normalBoostGauge += normalBoostDriftGaugeIncreaseSpeed * Time.deltaTime;

        if (normalBoostTotalGauge <= normalBoostGauge)
        {
            AddNormalBoost();

            normalBoostGauge = 0;
        }
    }

    // 일반 부스터 추가 함수 (최대 2개까지 소지 가능)
    void AddNormalBoost()
    {
        if (normalBoostMaxCount <= normalBoostCount)
            return;
        else if (normalBoostCount == 0)
            UIManager.Instance.NormalBoostButtonSetActive(1, true);
        else
            UIManager.Instance.NormalBoostButtonSetActive(2, true);
        
        ++normalBoostCount;
    }

    // 일반 부스터 발동 래퍼 함수
    public void NormalBoostExecute()
    {
        if (normalBoostCount <= 0)
            return;
        else if (normalBoostCount < normalBoostMaxCount)
            UIManager.Instance.NormalBoostButtonSetActive(1, false);
        else
            UIManager.Instance.NormalBoostButtonSetActive(2, false);

        Boost(Player.State.NormalBoost);
        AudioManager.Instance.PlayAudio(AudioManager.AudioType.NormalBooster);

        --normalBoostCount; // 부스터 소지 개수 감소

        ++totalBoostCount;   // 총 부스터 사용 횟수 누적
    }

    // 터치 부스터 게이지 Setter
    // 터치할 때마다 다음 터치를 기다리기 위해 누적 시간을 초기화
    public void SetTouchBoostGauge(int gauge)
    {
        touchBoostGauge = gauge;
        touchBoostKeepCurrentTime = 0;
    }

    // 터치 부스터 제어기
    // (터치 부스터는 화면을 3번 빠르게 터치하면 발동)
    // (touchBoostKeepAvailableTime 내에 다음 터치를 해야 누적 가능)
    public void TouchBoostController()
    {
        // 터치해서 게이지를 누적하는 중이면 시간을 누적
        if (0 < touchBoostGauge)
            touchBoostKeepCurrentTime += Time.deltaTime;

        // 터치 부스터를 사용할 수 없는 상황이거나, 일정 시간 내에 추가 터치하지 않으면 초기화
        if (!bTouchBoostAvailable || touchBoostKeepAvailableTime <= touchBoostKeepCurrentTime)
            SetTouchBoostGauge(0);

        // 화면을 빠르게 3번 터치해 게이지가 가득 찼으면 발동
        if (touchBoostTotalGauge <= touchBoostGauge)
        {
            Boost(Player.State.TouchBoost);
            AudioManager.Instance.PlayAudio(AudioManager.AudioType.TouchBooster);
            
            SetTouchBoostGauge(0);
        }
    }

    // 부스터 동시성 제어기
    void BoostConcurrencyController()
    {
        switch (Player.Instance.state)
        {
        case Player.State.NormalBoost :
            bMMTBoostAvailable = false;
            bTouchBoostAvailable = false;
            bNormalBoostAvailable = false;
            break;
        case Player.State.MMTBoost :
            bTouchBoostAvailable = false;
            break;
        case Player.State.JumpBox :
            bMMTBoostAvailable = false;
            bTouchBoostAvailable = false;
            bNormalBoostAvailable = false;
            break;
        }
    }
}
