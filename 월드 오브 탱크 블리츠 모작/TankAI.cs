using System.Linq;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;

// 간단한 FSM을 통해 AI 탱크를 동작시키는 컴포넌트이다.
// 타겟 탐색, 목적지 이동, 공격을 수행한다.
public class TankAI : MonoBehaviour
{
    public enum State
    {
        Idle,    // 목적지, 타겟의 초깃값을 설정하는 상태
        Move    // 목적지로 이동, 혹은 타겟을 공격하기 위해 이동하는 상태
    }
    State state

    // 새로운 타겟을 탐색(새로운 목적지를 설정)하는 간격
    public int patrolDuration = 20;
    // 목적지를 탐색할 때 이용하는 반경
    public int patrolDistance = 100;
    // 목적지로부터의 정지 거리
    public float stopDistance = 2;
    // 발사(공격) 쿨타임
    public float fireInterval = 5;
    // 발사(공격) 사거리
    public float fireDistance = 100;
    // 탄퍼짐 정도
    public int fireSpread = 5;
    // 데미지
    public float damage = 80;

    Tank tank;
    NavMeshAgent agent;
    Transform mapCenter;    // 맵 중앙 위치 저장
    GameObject runSmoke;    // 흙먼지 효과

    GameObject currentTarget;    // 현재 설정된 타겟 저장
    Vector3 destination;        // 이동 목적지
    Vector3 spreadOffset;        // fireSpread(탄퍼짐 정도)를 통해 생성한 탄퍼짐 벡터

    // 이동, 공격 간격을 위한 임시 시간 변수
    float aiPatrolTime = 0;
    [HideInInspector]
    public float aiFireTime = 0;

    void Start()
    {
        tank = GetComponent<Tank>();
        agent = GetComponent<NavMeshAgent>();
        agent.enabled = true;

        runSmoke = transform.Find("RunSmoke").gameObject;
        mapCenter = GameObject.Find("MapCenter").transform;

        aiFireTime = fireInterval;
    }


    void Update()
    {
        switch (state)
        {
        case State.Idle: UpdateIdle(); break;
        case State.Move: UpdateMove(); break;
        }

        // 발사(공격) 제어기
        // 레이와 타겟과의 각도차를 이용해, 조건을 만족하면 공격을 수행한다.
        FireController();
    }

    void UpdateIdle()
    {
        // 새로운 타겟 탐색
        currentTarget = FindNewTarget();

        // 타겟을 찾았으면
        if (currentTarget != null)
        {
            // 현재 타겟의 위치에서 반경 fireDistance 이내의 랜덤 지점을 목적지로 설정한다.
            destination = currentTarget.transform.position + new Vector3(Random.Range(-fireDistance, fireDistance), 0, Random.Range(-fireDistance, fireDistance));
            // 공격시 사용될 노이즈 벡터를 미리 초기화한다.
            spreadOffset = (new Vector3(Random.Range(-fireSpread, fireSpread), Random.Range(0, fireSpread) + targetDistance, Random.Range(-fireSpread, fireSpread)));
            
        }
        // 타겟을 못 찾았으면
        else
        {
            // 현재 위치에서 반경 patrolDistance 이내의 랜덤 지점을 목적지로 설정한다.
            if (!NewDestination(out destination))
                destination = DetectGround(transform.position + new Vector3(Random.Range(-patrolDistance, patrolDistance), 20, Random.Range(-patrolDistance, patrolDistance)));
        }

        state = State.Move;
    }

    void UpdateMove()
    {
        aiPatrolTime += Time.deltaTime;
        // 한 번의 텀이 끝났으면 다음 다시 타겟을 찾는다.
        if (patrolDuration <= aiPatrolTime)
        {
            aiPatrolTime = 0;
            state = State.Idle;
            return;
        }

        // 타겟이 없는 상태로 이동 중이거나, 타겟이 죽었으면
        if (currentTarget == null || !currentTarget.activeSelf)
        {
            // 새로운 타겟을 찾는다.
            GameObject newTarget = FindNewTarget();

            if (newTarget != null)
            {
                currentTarget = newTarget;
                destination = currentTarget.transform.position + new Vector3(Random.Range(-fireDistance, fireDistance), 0, Random.Range(-fireDistance, fireDistance));
                spreadOffset = (new Vector3(Random.Range(-fireSpread, fireSpread), Random.Range(0, fireSpread) + targetDistance, Random.Range(-fireSpread, fireSpread)));
            }
        }

        float distanceToDest = Vector3.Distance(transform.position, destination);

        if (stopDistance < distanceToDest)
        {
            agent.destination = destination;
            agent.enabled = true;
            runSmoke.SetActive(true);

            // 이동 중 벽을 지나려고해 막히거나, 장애물에 걸렸을 경우,
            // 새로운 목적지를 설정해 빠져나오기 위한 함수다.
            ObstacleEscaper();
        }
        else
        {
            agent.enabled = false;
            runSmoke.SetActive(false);
        }
    }

    // 발사(공격) 제어기
    // 레이와 타겟과의 각도차를 이용해, 조건을 만족하면 공격을 수행한다.
    void FireController()
    {
        if (currentTarget == null)
            return;

        float targetDistance = Vector3.Distance(transform.position, currentTarget.transform.position);
        Vector3 targetDirection = (currentTarget.transform.position - transform.position).normalized;

        RaycastHit hit;
        bool canFire = false; 
        // 타겟 위에서 내 방향으로 Ray를 쏴서
        if (Physics.Raycast(currentTarget.transform.position + Vector3.up - (targetDirection * 2), -targetDirection, out hit))
            // 내가 맞았고
            if (hit.collider.gameObject == gameObject)
                // 공격 가능 거리 안에 있으면
                if (targetDistance <= fireDistance)
                    // 발사 가능 상태로 설정
                    canFire = true;

        // 타겟의 위치에 노이즈를 더한 위치로 포탑과 포신을 서서히 회전시킨다.
        tank.Aim(currentTarget.transform, spreadOffset);

        aiFireTime += Time.deltaTime;
        // 포신과 타겟의 가로, 세로 각도 차의 합이 5 미만일 때
        if (canFire && tank.AimingAngle < 5)
        {
            // 발사 쿨타임이 돌았으면
            if (fireInterval <= aiFireTime)
            {
                // 총알 혹은 포탄 발사
                if (Random.value < 0.5f)
                    tank.cannon.Shoot(Canon.Weapon.Bullet);
                else
                    tank.cannon.Shoot(Canon.Weapon.Projectile);
                
                aiFireTime = 0;
            }
        }
    }

    // 다음 타겟을 찾아 currentTarget에 설정하는 함수
    GameObject FindNewTarget()
    {
        // 자신을 마지막으로 때린 탱크를 50% 확률로 다음 타겟으로 설정
        HPManager tankHPManager = GetComponent<HPManager>();

        if (Random.value < 0.5f && tankHPManager)
            if (tankHPManager.LatestHit && tankHPManager.LatestHit.activeSelf)
                return tankHPManager.LatestHit;

        // 상대 팀의 탱크 목록을 풀에서 가져온다.
        string opponentTag = TagObjectFinder.Instance.OpponentTag(gameObject.tag);
        GameObject[] targets = TagObjectFinder.Instance.FindObjectWithTag(opponentTag);
        newTarget = null;

        // 가장 가까운 탱크를 타겟으로 지정한다.
        if (targets != null)
        {
            float minDistance = float.MaxValue;

            foreach (GameObject target in targets)
            {
                // 자기 자신이 아니고
                if (target != null && target != gameObject)
                {
                    float distance = Vector3.Distance(transform.position, target.transform.position);
                    
                    // 현재 타겟과의 거리보다 가까우면
                    if (distance < minDistance)
                    {
                        // 새로운 타겟으로 갱신한다.
                        newTarget = target;
                        minDistance = distance;
                    }
                }
            }
        }

        return newTarget;
    }

    float t;
    Vector3 prevPos;
    Queue<float> queDeltaPosition = new Queue<float>();
    // 길찾기 중 벽을 지나려고해 막히거나, 모서리에 걸렸을 경우 빠져나오기 위한 함수다.
    void ObstacleEscaper()
    {
        // 큐에는 최근 2초 동안의 모든 이동 간격을 저장한다.
        queDeltaPosition.Enqueue(Vector3.Distance(prevPos, transform.position));

        prevPos = transform.position;
        // 시작 후 2초 후부터 확인한다.
        if (t < 2)
        {
            t += Time.deltaTime;
        }
        else
        {
            // 2초 동안의 이동 간격의 합(이동 거리)이 2unit 미만이면, 걸렸다고 판단한다.
            if (queDeltaPosition.Sum() < 2)
            {
                Vector3 newDest;

                if (!NewDestination(out newDest))
                    newDest = DetectGround(transform.position + new Vector3(Random.Range(-patrolDistance, patrolDistance), 20, Random.Range(-patrolDistance, patrolDistance)));                

                agent.destination = newDest;
            }

            // 가장 오래된 이동 간격을 큐에서 pop해, 큐의 길이를 일정하게 유지한다.
            queDeltaPosition.Dequeue();
        }
    }

    // position에서의 바닥 위치 반환
    Vector3 DetectGround(Vector3 position)
    {
        RaycastHit hit;

        if (Physics.Raycast(position, -Vector3.up, out hit, float.MaxValue))
            return hit.point;
            
        return position;
    }

    // 자기장 필드 내의 맵 영역에서 다음 랜덤 목적지를 찾는다.
    bool NewDestination(out Vector3 newPosition)
    {
        float fieldRadius = MGFieldDamageManager.Instance.fieldDistance;

        NavMeshHit hitInfo;
        bool bNewDestinationSet = false;

        Vector3 point = mapCenter.position + Random.insideUnitSphere * fieldRadius;
        point.y = mapCenter.position.y;

        bNewDestinationSet = NavMesh.SamplePosition(point, out hitInfo, fieldRadius, 1);

        newPosition = (bNewDestinationSet) ? hitInfo.position : Vector3.zero;

        return bNewDestinationSet;
    }
}
