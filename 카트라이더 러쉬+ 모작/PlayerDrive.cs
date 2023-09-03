using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 플레이어 카트 주행 컴포넌트
public class PlayerDrive : MonoBehaviour
{
    // 이동 속력
    public float moveSpeed = 50f;
    // 최대 이동 속력
    public float maxSpeed = 15f;
    // 현재 속력
    public float currentSpeed;
    // 최대 속력 초깃값
    public float o_maxSpeed;
    // 핸들링 민감도
    public float handleSpeed = 0.2f;
    // 드리프트 민감도
    public float steerAngle = 20f;
    // 드리프트 시 속력이 줄어드는 마찰 계수 (deltaTime 당)
    public float drag;
    // 드리프트 종료 후 정방향 보정에 걸리는 시간
    public float driftLerpTime;
    // 드리프트 사용 가능 여부
    public bool bDriftAvailable;
    // 브레이크(후진) 여부
    public bool bBrake;
    // 적용될 중력의 배수
    public float gravityFactor;

    CharacterController cc;
    // 부스터 컴포넌트
    Boost boost;
    // 이동 방향 캐싱 (후진일 경우 뒤로)
    Vector3 moveDirection;
    // 추진력
    Vector3 moveForce;
    // 현재 속력을 계산하기 위해 이전 위치를 저장하는 변수
    Vector3 prevPos;
    // 중력 가속도
    float gravity = 9.81f;
    // 자유 낙하 속도 누적 값
    float yVelocity;
    // "Horizontal" 값 캐싱
    float steerInput;

    void Start()
    {
        cc = GetComponent<CharacterController>();
        boost = GetComponent<Boost>();

        o_maxSpeed = maxSpeed;

        bDriftAvailable = true;
    }

    void Update()
    {
        // 준비, 종료, 점프대 사용 시에는 조작 불가
        if ((Player.Instance.state & (Player.State.Ready | Player.State.End | Player.State.JumpBox)) != 0)
            return;

        // 후진이면 뒤 방향
        moveDirection = transform.forward * (bBrake ? -1 : 1);

        // 추진력에 벡터를 누적
        moveForce += moveDirection * moveSpeed * boost.boostSpeedFactor * Time.deltaTime;
        moveForce = Vector3.ClampMagnitude(moveForce, maxSpeed);

        steerInput = Input.GetAxisRaw("Horizontal");

        // 드리프트 제어기
        DriftController();

        // 중력 가속도 적용
        if (cc.isGrounded)
            yVelocity = 0;
        else
            yVelocity += gravity * gravityFactor * Time.deltaTime;

        // 누적한 추진력과 중력 가속도에 따라 위치 이동
        cc.Move((moveForce + Vector3.down * yVelocity) * Time.deltaTime);

        // 기본 주행시 핸들링 (후진 시에는 좌우가 반대가 된다.)
        if (Player.Instance.state != Player.State.Drift)
            transform.Rotate(Vector3.up * handleSpeed * steerInput * (bBrake ? -1 : 1));

        // 현재 속력 계산
        currentSpeed = Vector3.Distance(prevPos, transform.position) / Time.deltaTime;
        prevPos = transform.position;

        // 순간(Momentary) 부스터 활성화기
        mmtBoostEnabler();
    }

    // driftLerpTime까지 시간을 누적하기 위한 변수
    float currentLerpTime;
    // 드리프트 제어기
    void DriftController()
    {
        // 이미 드리프트 중이거나 후진 중에는 불가
        if (Player.Instance.state == Player.State.Drift || bBrake)
        {
            currentLerpTime = 0;
        }
        // 드리프트 중이 아니면
        else
        {
            currentLerpTime += Time.deltaTime;

            if (currentLerpTime < driftLerpTime)    // 드리프트 보정 시간 동안 정방향 보정 진행
                moveForce = Vector3.Lerp(moveForce.normalized, moveDirection, currentLerpTime / driftLerpTime) * moveForce.magnitude;
            else    // 보정 시간이 끝났으면 추진력을 이동 방향으로 설정
                moveForce = moveDirection * moveForce.magnitude;
        }
        
        // 드리프트 중이면
        if (Player.Instance.state == Player.State.Drift)
        {
            // 현재 추진력의 크기에 따라 드리프트 진행
            transform.Rotate(Vector3.up * steerInput * (bBrake ? -1 : 1) * moveForce.magnitude * steerAngle * Time.deltaTime);
            // 마찰 계수 적용
            moveForce *= drag;
        }
    }

    // 드리프트를 한 누적 시간
    float currentDriftTime;
    // 순간 부스터 활성화기
    void mmtBoostEnabler()
    {
        // 드리프트 중이면 시간을 누적한다.
        if (Player.Instance.state == Player.State.Drift)
        {
            currentDriftTime += Time.deltaTime;
        }
        // 드리프트가 끝났을 때
        else
        {
            // 누적 시간을 충족하면 순간 부스터를 활성화한다.
            if (boost.mmtBoostRequireTime <= currentDriftTime)
                boost.EnableMMTBoost();

            currentDriftTime = 0;
        }
    }
}
